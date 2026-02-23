package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.entity.Booking;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.PaymentStatus;
import com.nukkadseva.nukkadsevabackend.exception.BookingCreationException;
import com.nukkadseva.nukkadsevabackend.exception.CustomerNotFoundException;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.mapper.BookingMapper;
import com.nukkadseva.nukkadsevabackend.repository.BookingRepository;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingNotificationDto;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingResponseDto;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void createBooking(BookingRequest bookingRequest, Authentication authentication) {
        String email = authentication.getName();

        Customers customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Provider provider = providerRepository.findById(bookingRequest.getProviderId())
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));
        if (provider == null) {
            log.error("Provider not found {}", bookingRequest.getProviderId());
        }

        Booking booking = bookingMapper.toEntity(bookingRequest);
        booking.setCustomer(customer);
        booking.setProvider(provider);

        if (bookingRequest.getBookingDateTime().isAfter(LocalDateTime.now().plusDays(7))) {
            throw new BookingCreationException("Booking cannot be scheduled more than 7 days in advance.");
        }
        booking.setBookingDateTime(bookingRequest.getBookingDateTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        try {
            bookingRepository.save(booking);

            // Send real-time notification to the provider
            BookingNotificationDto notification = BookingNotificationDto.builder()
                    .bookingId(booking.getId().toString())
                    .customerName(customer.getFullName())
                    .serviceType(booking.getServiceType().name())
                    .bookingDateTime(booking.getBookingDateTime())
                    .priceEstimate(booking.getPriceEstimate() != null ? booking.getPriceEstimate().doubleValue() : null)
                    .note(booking.getNote())
                    .status(booking.getStatus().name())
                    .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().toString()
                            : LocalDateTime.now().toString())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    provider.getEmail(),
                    "/queue/bookings",
                    notification);
            log.info("Sent real-time notification to provider: {}", provider.getEmail());

        } catch (CustomerNotFoundException | ProviderNotFoundException e) {
            log.error("Not found exception creating booking: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error creating booking", e);
            throw new BookingCreationException("Booking failed due to database error!", e);
        } catch (Exception e) {
            log.error("Unexpected error creating booking", e);
            throw new BookingCreationException(
                    "An unexpected error occurred while creating the booking: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BookingResponseDto> getCustomerBookings(Authentication authentication) {
        String email = authentication.getName();
        Customers customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        List<Booking> bookings = bookingRepository.findByCustomerOrderByCreatedAtDesc(customer);
        return bookings.stream().map(this::mapToBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getProviderBookings(Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        List<Booking> bookings = bookingRepository.findByProviderOrderByCreatedAtDesc(provider);
        return bookings.stream().map(this::mapToBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void respondToBooking(UUID id, String action, String reason, Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: You are not the provider for this booking");
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            booking.setStatus(BookingStatus.APPROVED);
            // Generate a 4-digit OTP for completion
            String otp = String.format("%04d", new java.util.Random().nextInt(10000));
            booking.setCompletionOtp(otp);
        } else if ("REJECT".equalsIgnoreCase(action) || "DECLINE".equalsIgnoreCase(action)) {
            booking.setStatus(BookingStatus.REJECTED);
            if (reason != null && !reason.trim().isEmpty()) {
                booking.setRejectionReason(reason.trim());
            }
        } else if ("COMPLETE".equalsIgnoreCase(action)) {
            // Note: Provider should now use completeBookingWithOtp instead, but we keep
            // this for backwards compatibility or admin override.
            booking.setStatus(BookingStatus.COMPLETED);
            booking.setCompletedAt(LocalDateTime.now());
        } else {
            throw new RuntimeException("Invalid action");
        }

        bookingRepository.save(booking);
        sendCustomerNotification(booking);
    }

    @Override
    @Transactional
    public void cancelCustomerBooking(UUID id, Authentication authentication) {
        String email = authentication.getName();
        Customers customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized: You are not the customer for this booking");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.REJECTED
                || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking cannot be cancelled in its current state");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Notify provider that customer cancelled
        sendProviderNotification(booking);
        // Notify customer as well for UI updates
        sendCustomerNotification(booking);
    }

    @Override
    @Transactional
    public void completeBookingWithOtp(UUID id, String otp, Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: You are not the provider for this booking");
        }

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Booking must be APPROVED before it can be completed");
        }

        if (booking.getCompletionOtp() == null || !booking.getCompletionOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid Completion OTP");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Notify customer that the job was completed
        sendCustomerNotification(booking);
    }

    private void sendProviderNotification(Booking booking) {
        BookingNotificationDto notification = BookingNotificationDto.builder()
                .bookingId(booking.getId().toString())
                .customerName(booking.getCustomer().getFullName())
                .serviceType(booking.getServiceType().name())
                .bookingDateTime(booking.getBookingDateTime())
                .priceEstimate(booking.getPriceEstimate() != null ? booking.getPriceEstimate().doubleValue() : null)
                .note(booking.getNote())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().toString()
                        : LocalDateTime.now().toString())
                .rejectionReason(booking.getRejectionReason())
                .build();
        messagingTemplate.convertAndSendToUser(
                booking.getProvider().getEmail(),
                "/queue/bookings",
                notification);
    }

    private void sendCustomerNotification(Booking booking) {
        BookingNotificationDto notification = BookingNotificationDto.builder()
                .bookingId(booking.getId().toString())
                .customerName(booking.getProvider().getFullName())
                .serviceType(booking.getServiceType().name())
                .bookingDateTime(booking.getBookingDateTime())
                .priceEstimate(booking.getPriceEstimate() != null ? booking.getPriceEstimate().doubleValue() : null)
                .note(booking.getNote())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().toString()
                        : LocalDateTime.now().toString())
                .completionOtp(booking.getCompletionOtp())
                .rejectionReason(booking.getRejectionReason())
                .build();

        messagingTemplate.convertAndSendToUser(
                booking.getCustomer().getEmail(),
                "/queue/customer-bookings",
                notification);
    }

    private BookingResponseDto mapToBookingResponseDto(Booking booking) {
        BookingResponseDto.CustomerSummary customerSummary = BookingResponseDto.CustomerSummary.builder()
                .name(booking.getCustomer().getFullName())
                .phone(booking.getCustomer().getMobileNumber())
                .address(
                        booking.getCustomer().getAddress() != null ? booking.getCustomer().getAddress().getFullAddress()
                                : null)
                .build();

        BookingResponseDto.ProviderSummary providerSummary = BookingResponseDto.ProviderSummary.builder()
                .name(booking.getProvider().getFullName())
                .businessName(booking.getProvider().getBusinessName())
                .contactNumber(booking.getProvider().getMobileNumber())
                .build();

        return BookingResponseDto.builder()
                .id(booking.getId())
                .serviceType(booking.getServiceType() != null ? booking.getServiceType().name() : null)
                .bookingDateTime(booking.getBookingDateTime())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .priceEstimate(booking.getPriceEstimate())
                .note(booking.getNote())
                .createdAt(booking.getCreatedAt())
                .completionOtp(booking.getCompletionOtp())
                .rejectionReason(booking.getRejectionReason())
                .isReviewed(booking.getReview() != null)
                .rating(booking.getReview() != null ? (int) booking.getReview().getRating() : null)
                .customer(customerSummary)
                .provider(providerSummary)
                .build();
    }
}
