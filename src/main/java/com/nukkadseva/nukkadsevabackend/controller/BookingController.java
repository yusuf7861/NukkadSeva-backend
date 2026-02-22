package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.ApiResponse;
import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingResponseDto;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse> createBooking(@RequestBody BookingRequest bookingRequest,
            Authentication authentication) {
        log.info("Received booking request: {}", bookingRequest);
        bookingService.createBooking(bookingRequest, authentication);
        return ResponseEntity.ok(new ApiResponse("CREATED", "Booking Completed Successfully"));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<List<BookingResponseDto>> getCustomerBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getCustomerBookings(authentication));
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<BookingResponseDto>> getProviderBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getProviderBookings(authentication));
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PutMapping("/{id}/respond")
    public ResponseEntity<ApiResponse> respondToBooking(
            @PathVariable UUID id,
            @RequestParam String action,
            Authentication authentication) {
        bookingService.respondToBooking(id, action, authentication);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", "Booking action completed successfully"));
    }
}
