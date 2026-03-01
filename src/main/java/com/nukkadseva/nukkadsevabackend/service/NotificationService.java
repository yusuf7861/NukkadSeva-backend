package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.response.BookingNotificationDto;

public interface NotificationService {
    void sendBookingNotificationToProvider(String providerEmail, BookingNotificationDto notification);

    void sendBookingNotificationToCustomer(String customerEmail, BookingNotificationDto notification);
}
