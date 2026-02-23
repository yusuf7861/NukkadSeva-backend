package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.ReviewRequestDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ReviewResponseDto;
import com.nukkadseva.nukkadsevabackend.entity.Booking;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.Review;
import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.repository.BookingRepository;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.ReviewRepository;
import com.nukkadseva.nukkadsevabackend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public ReviewResponseDto submitReview(ReviewRequestDto request, Authentication authentication) {
        String email = authentication.getName();
        Customers customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Validate booking belongs to customer
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Not authorized to review this booking");
        }

        // Validate booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Only completed bookings can be reviewed");
        }

        // Check if review already exists
        if (booking.getReview() != null || reviewRepository.existsByBookingId(booking.getId())) {
            throw new RuntimeException("Review already exists for this booking");
        }

        // Create and save the review
        Review review = new Review();
        review.setCustomer(customer);
        review.setProvider(booking.getProvider());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setBooking(booking);

        Review savedReview = reviewRepository.save(review);

        // Link review back to booking
        booking.setReview(savedReview);
        bookingRepository.save(booking);

        return ReviewResponseDto.builder()
                .id(savedReview.getId())
                .bookingId(savedReview.getBooking().getId())
                .customerId(savedReview.getCustomer().getId())
                .customerName(savedReview.getCustomer().getFullName())
                .providerId(savedReview.getProvider().getId())
                .rating(savedReview.getRating())
                .comment(savedReview.getComment())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    @Override
    public List<ReviewResponseDto> getProviderReviews(Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        return reviewRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(r -> ReviewResponseDto.builder()
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
    }
}
