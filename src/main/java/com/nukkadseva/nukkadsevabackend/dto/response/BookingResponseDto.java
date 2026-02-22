package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.PaymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private UUID id;
    private String serviceType;
    private LocalDateTime bookingDateTime;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal priceEstimate;
    private String note;
    private LocalDateTime createdAt;

    private CustomerSummary customer;
    private ProviderSummary provider;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummary {
        private String name;
        private String phone;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderSummary {
        private String name;
        private String businessName;
        private String contactNumber;
    }
}
