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
public class ProviderDashboardDto {
    private BigDecimal totalEarnings;
    private long completedJobs;
    private double averageRating;
    private long pendingRequestsCount;
    private List<PendingBookingItem> pendingBookings;
    private List<ReviewResponseDto> recentReviews;
    private List<PastServiceItem> recentPastServices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PastServiceItem {
        private String bookingId;
        private String customerName;
        private String serviceType;
        private String bookingDateTime;
        private BigDecimal finalPrice;
        private String status;
        private String completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingBookingItem {
        private String bookingId;
        private String customerName;
        private String serviceType;
        private String bookingDateTime;
        private BigDecimal priceEstimate;
        private String note;
        private String status;
        private String createdAt;
    }
}
