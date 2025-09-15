package com.nukkadseva.nukkadsevabackend.dto.response;

import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProviderSummaryDto {
    private Long id;
    private String fullName;
    private String businessName;
    private String mobileNumber;
    private ProviderStatus status;
    private LocalDateTime createdAt;
}