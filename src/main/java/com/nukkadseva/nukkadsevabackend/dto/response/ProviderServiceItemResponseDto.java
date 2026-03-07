package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProviderServiceItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int durationMinutes;
    private boolean isActive;
}
