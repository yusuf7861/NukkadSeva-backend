package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ServiceSearchResultDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer durationMinutes;
    private String providerName;
    private Long providerId;
    private List<String> pincodes;
    private boolean providerVerified;
}
