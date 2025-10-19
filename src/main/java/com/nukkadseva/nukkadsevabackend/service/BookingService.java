package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import org.springframework.security.core.Authentication;

public interface BookingService {
    void createBooking(BookingRequest bookingRequest, Authentication authentication);
}
