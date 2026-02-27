package com.nukkadseva.nukkadsevabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class ProviderAreaRequest {
    @NotBlank(message = "City is required")
    private String city;

    @NotEmpty(message = "At least one pincode must be provided")
    private Set<String> pincodes;
}
