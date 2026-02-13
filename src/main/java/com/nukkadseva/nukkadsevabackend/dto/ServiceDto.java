package com.nukkadseva.nukkadsevabackend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer durationMinutes;
    private boolean isActive;
    private Long providerId;
    private String providerName;
    private java.util.Set<String> pincodes;
    private boolean isProviderVerified;
}
