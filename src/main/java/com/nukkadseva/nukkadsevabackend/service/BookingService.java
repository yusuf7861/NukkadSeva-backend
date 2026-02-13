package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingNotificationDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    void createBooking(BookingRequest bookingRequest, Authentication authentication);

    List<BookingNotificationDto> getProviderPendingBookings(Authentication authentication);

    void respondToBooking(UUID bookingId, String action, Authentication authentication);
}
