package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.response.BookingNotificationDto;
import com.nukkadseva.nukkadsevabackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendBookingNotificationToProvider(String providerEmail, BookingNotificationDto notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    providerEmail,
                    "/queue/bookings",
                    notification);
            log.info("Sent real-time notification to provider: {}", providerEmail);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to provider: {}", providerEmail, e);
        }
    }

    @Override
    public void sendBookingNotificationToCustomer(String customerEmail, BookingNotificationDto notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    customerEmail,
                    "/queue/customer/bookings",
                    notification);
            log.info("Sent real-time notification to customer: {}", customerEmail);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to customer: {}", customerEmail, e);
        }
    }
}
