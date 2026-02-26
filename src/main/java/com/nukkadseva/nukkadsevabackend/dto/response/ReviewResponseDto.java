package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponseDto {
    private Long id;
    private UUID bookingId;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private byte rating;
    private String comment;
    private LocalDateTime createdAt;
}
