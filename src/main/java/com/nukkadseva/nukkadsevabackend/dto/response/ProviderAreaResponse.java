package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ProviderAreaResponse {
    private Long id;
    private String city;
    private Set<String> pincodes;
    private LocalDateTime createdAt;
}
