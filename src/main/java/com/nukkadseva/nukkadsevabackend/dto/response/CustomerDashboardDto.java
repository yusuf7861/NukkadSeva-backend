package com.nukkadseva.nukkadsevabackend.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboardDto {
    private long totalBookings;
    private BigDecimal totalSpent;
    private long pendingBookings;
    private double averageRating;
    private List<BookingItem> recentBookings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingItem {
        private String id;
        private String serviceName;
        private String providerName;
        private String bookingDate;
        private String status;
        private BigDecimal amount;
    }
}
