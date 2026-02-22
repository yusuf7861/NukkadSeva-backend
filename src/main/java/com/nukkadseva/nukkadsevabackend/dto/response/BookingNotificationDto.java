package com.nukkadseva.nukkadsevabackend.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingNotificationDto {
    private String bookingId;
    private String customerName;
    private String serviceType;
    private LocalDateTime bookingDateTime;
    private Double priceEstimate;
    private String note;
    private String status;
    private String createdAt;
}
