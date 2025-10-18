package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.ApiResponse;
import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse> createBooking(@RequestBody BookingRequest bookingRequest, Authentication authentication) {
        bookingService.createBooking(bookingRequest, authentication);
        return ResponseEntity.ok(new ApiResponse("CREATED", "Booking Completed Successfully"));
    }
}
