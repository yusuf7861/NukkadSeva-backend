package com.nukkadseva.nukkadsevabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityWithPincodesRequest {

    @NotBlank(message = "City name is required")
    private String cityName;

    @NotBlank(message = "State is required")
    private String state;

    @NotEmpty(message = "At least one pincode is required")
    private List<PincodeRequest> pincodes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PincodeRequest {
        @NotBlank(message = "Pincode is required")
        private String pincode;

        private String areaName;
    }
}

