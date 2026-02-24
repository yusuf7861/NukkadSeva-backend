package com.nukkadseva.nukkadsevabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerAddressDto {
    private Long id;

    @NotBlank(message = "Type is required, e.g., HOME, WORK")
    private String type;

    private String flatName;

    @NotBlank(message = "Area is required")
    private String area;

    private String landmark;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private boolean isDefault;
}
