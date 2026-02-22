package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import org.springframework.security.core.Authentication;
import com.nukkadseva.nukkadsevabackend.dto.response.BookingResponseDto;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    void createBooking(BookingRequest bookingRequest, Authentication authentication);

    List<BookingResponseDto> getCustomerBookings(Authentication authentication);

    List<BookingResponseDto> getProviderBookings(Authentication authentication);

    void respondToBooking(UUID id, String action, Authentication authentication);
}
