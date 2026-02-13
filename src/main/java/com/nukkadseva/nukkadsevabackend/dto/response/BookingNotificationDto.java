package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingNotificationDto {
    private String bookingId;
    private String customerName;
    private String serviceType;
    private String bookingDateTime;
    private BigDecimal priceEstimate;
    private String note;
    private String status;
    private String createdAt;
}
