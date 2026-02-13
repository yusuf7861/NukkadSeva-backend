package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.ApiResponse;
import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingNotificationDto;
import com.nukkadseva.nukkadsevabackend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse> createBooking(@RequestBody BookingRequest bookingRequest,
            Authentication authentication) {
        bookingService.createBooking(bookingRequest, authentication);
        return ResponseEntity.ok(new ApiResponse("CREATED", "Booking Completed Successfully"));
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/pending")
    public ResponseEntity<List<BookingNotificationDto>> getProviderPendingBookings(Authentication authentication) {
        List<BookingNotificationDto> bookings = bookingService.getProviderPendingBookings(authentication);
        return ResponseEntity.ok(bookings);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/{bookingId}/respond")
    public ResponseEntity<ApiResponse> respondToBooking(
            @PathVariable UUID bookingId,
            @RequestParam String action,
            Authentication authentication) {
        bookingService.respondToBooking(bookingId, action, authentication);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", "Booking " + action.toLowerCase() + "ed successfully"));
    }
}
