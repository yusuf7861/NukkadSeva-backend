package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingNotificationDto;
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
import com.nukkadseva.nukkadsevabackend.security.AuthUser;
import com.nukkadseva.nukkadsevabackend.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void createBooking(BookingRequest bookingRequest, Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        String email = authUser.getEmail();

        Customers customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Provider provider = providerRepository.findById(bookingRequest.getProviderId())
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

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

            // Send real-time notification to the provider via WebSocket
            BookingNotificationDto notification = toNotificationDto(booking);
            String providerProfileId = String.valueOf(provider.getId());
            messagingTemplate.convertAndSendToUser(
                    providerProfileId,
                    "/queue/bookings",
                    notification);
            log.info("Sent booking notification to provider {} for booking {}", providerProfileId, booking.getId());

        } catch (CustomerNotFoundException | ProviderNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new BookingCreationException("Booking failed due to database error!", e);
        } catch (Exception e) {
            throw new BookingCreationException("An unexpected error occurred while creating the booking", e);
        }
    }

    @Override
    public List<BookingNotificationDto> getProviderPendingBookings(Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        Long profileId = authUser.getProfileId();

        List<Booking> pendingBookings = bookingRepository
                .findByProviderIdAndStatusOrderByCreatedAtDesc(profileId, BookingStatus.PENDING);

        return pendingBookings.stream()
                .map(this::toNotificationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void respondToBooking(UUID bookingId, String action, Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        Long profileId = authUser.getProfileId();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingCreationException("Booking not found"));

        // Ensure the provider owns this booking
        if (!booking.getProvider().getId().equals(profileId)) {
            throw new BookingCreationException("You are not authorized to respond to this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingCreationException("Booking has already been responded to");
        }

        switch (action.toUpperCase()) {
            case "ACCEPT":
                booking.setStatus(BookingStatus.APPROVED);
                break;
            case "DECLINE":
                booking.setStatus(BookingStatus.REJECTED);
                booking.setCancelledAt(LocalDateTime.now());
                break;
            default:
                throw new BookingCreationException("Invalid action. Use ACCEPT or DECLINE.");
        }

        bookingRepository.save(booking);
        log.info("Provider {} {} booking {}", profileId, action, bookingId);
    }

    /**
     * Convert Booking entity to BookingNotificationDto.
     */
    private BookingNotificationDto toNotificationDto(Booking booking) {
        return BookingNotificationDto.builder()
                .bookingId(booking.getId().toString())
                .customerName(booking.getCustomer().getFullName())
                .serviceType(booking.getServiceType().name())
                .bookingDateTime(booking.getBookingDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .priceEstimate(booking.getPriceEstimate())
                .note(booking.getNote())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt() != null
                        ? booking.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : null)
                .build();
    }
}
