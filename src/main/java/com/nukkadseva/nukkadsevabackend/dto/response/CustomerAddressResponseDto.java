package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerAddressResponseDto {
    private Long id;
    private String type;
    private String flatName;
    private String area;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private boolean isDefault;
}
