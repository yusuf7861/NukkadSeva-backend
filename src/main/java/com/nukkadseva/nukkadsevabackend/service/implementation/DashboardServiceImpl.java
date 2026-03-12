package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.response.CustomerDashboardDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderDashboardDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ReviewResponseDto;
import com.nukkadseva.nukkadsevabackend.entity.Booking;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.exception.CustomerNotFoundException;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.repository.BookingRepository;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.ReviewRepository;
import com.nukkadseva.nukkadsevabackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

        private final CustomerRepository customerRepository;
        private final ProviderRepository providerRepository;
        private final BookingRepository bookingRepository;
        private final ReviewRepository reviewRepository;

        @Override
        @Transactional(readOnly = true)
        public CustomerDashboardDto getCustomerDashboard(Authentication authentication) {
                String email = authentication.getName();
                Customers customer = customerRepository.findByEmail(email)
                                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

                List<Booking> allBookings = bookingRepository.findByCustomerOrderByCreatedAtDesc(customer);

                long totalBookings = allBookings.size();

                long pendingBookings = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.PENDING
                                                || b.getStatus() == BookingStatus.APPROVED)
                                .count();

                BigDecimal totalSpent = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                                .map(b -> b.getFinalPrice() != null ? b.getFinalPrice()
                                                : (b.getPriceEstimate() != null ? b.getPriceEstimate()
                                                                : BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Double avgRatingResult = reviewRepository.getAverageRatingForCustomer(customer.getId());
                double averageRating = avgRatingResult != null ? Math.round(avgRatingResult * 10.0) / 10.0 : 0.0;

                // Take top 5 recent bookings
                List<CustomerDashboardDto.BookingItem> recentBookings = allBookings.stream()
                                .limit(5)
                                .map(b -> CustomerDashboardDto.BookingItem.builder()
                                                .id(b.getId().toString())
                                                .serviceName(b.getServiceType() != null
                                                                ? b.getServiceType().name()
                                                                : "")
                                                .providerName(b.getProvider().getFullName())
                                                .bookingDate(b.getBookingDateTime() != null
                                                                ? b.getBookingDateTime().toString()
                                                                : "")
                                                .status(b.getStatus() != null ? b.getStatus().name() : "")
                                                .amount(b.getFinalPrice() != null ? b.getFinalPrice()
                                                                : b.getPriceEstimate())
                                                .build())
                                .collect(Collectors.toList());

                return CustomerDashboardDto.builder()
                                .totalBookings(totalBookings)
                                .totalSpent(totalSpent)
                                .pendingBookings(pendingBookings)
                                .averageRating(averageRating)
                                .recentBookings(recentBookings)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public ProviderDashboardDto getProviderDashboard(Authentication authentication) {
                String email = authentication.getName();
                Provider provider = providerRepository.findByEmail(email)
                                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

                List<Booking> allBookings = bookingRepository.findByProviderOrderByCreatedAtDesc(provider);

                long completedJobs = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                                .count();

                BigDecimal totalEarnings = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                                .map(b -> b.getFinalPrice() != null ? b.getFinalPrice()
                                                : (b.getPriceEstimate() != null ? b.getPriceEstimate()
                                                                : BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Take pending requests (strictly PENDING status)
                List<ProviderDashboardDto.PendingBookingItem> pendingBookings = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                                .map(b -> {
                                        return ProviderDashboardDto.PendingBookingItem.builder()
                                                        .bookingId(b.getId().toString())
                                                        .customerName(b.getCustomer().getFullName())
                                                        .serviceType(b.getServiceType() != null
                                                                        ? b.getServiceType().name()
                                                                        : "")
                                                        .bookingDateTime(b.getBookingDateTime() != null
                                                                        ? b.getBookingDateTime().toString()
                                                                        : "")
                                                        .priceEstimate(b.getPriceEstimate())
                                                        .note(b.getNote())
                                                        .status(b.getStatus() != null ? b.getStatus().name() : "")
                                                        .createdAt(b.getCreatedAt() != null
                                                                        ? b.getCreatedAt().toString()
                                                                        : "")
                                                        .build();
                                })
                                .collect(Collectors.toList());

                long pendingRequestsCount = pendingBookings.size();

                Double avgRatingResult = reviewRepository.getAverageRatingForProvider(provider.getId());
                double averageRating = avgRatingResult != null ? Math.round(avgRatingResult * 10.0) / 10.0 : 0.0;

                // Fetch Recent Past Services (up to 3 completed)
                List<ProviderDashboardDto.PastServiceItem> recentPastServices = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                                .limit(3)
                                .map(b -> ProviderDashboardDto.PastServiceItem.builder()
                                                .bookingId(b.getId().toString())
                                                .customerName(b.getCustomer().getFullName())
                                                .serviceType(b.getServiceType() != null ? b.getServiceType().name()
                                                                : "")
                                                .bookingDateTime(b.getBookingDateTime() != null
                                                                ? b.getBookingDateTime().toString()
                                                                : "")
                                                .finalPrice(b.getFinalPrice())
                                                .status(b.getStatus() != null ? b.getStatus().name() : "")
                                                // Using updated at or created at as a proxy for completed at for now
                                                .completedAt(b.getUpdatedAt() != null ? b.getUpdatedAt().toString()
                                                                : (b.getCreatedAt() != null
                                                                                ? b.getCreatedAt().toString()
                                                                                : ""))
                                                .build())
                                .collect(Collectors.toList());

                // Fetch Recent Reviews (up to 3)
                List<ReviewResponseDto> recentReviews = reviewRepository
                                .findByProviderIdOrderByCreatedAtDesc(provider.getId())
                                .stream()
                                .limit(3)
                                .map(r -> com.nukkadseva.nukkadsevabackend.dto.response.ReviewResponseDto.builder()
                                                .id(r.getId())
                                                .bookingId(r.getBooking().getId())
                                                .customerId(r.getCustomer().getId())
                                                .customerName(r.getCustomer().getFullName())
                                                .providerId(r.getProvider().getId())
                                                .rating(r.getRating())
                                                .comment(r.getComment())
                                                .createdAt(r.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                return ProviderDashboardDto.builder()
                                .totalEarnings(totalEarnings)
                                .completedJobs(completedJobs)
                                .pendingRequestsCount(pendingRequestsCount)
                                .pendingBookings(pendingBookings)
                                .averageRating(averageRating)
                                .recentReviews(recentReviews)
                                .recentPastServices(recentPastServices)
                                .build();
        }
}
