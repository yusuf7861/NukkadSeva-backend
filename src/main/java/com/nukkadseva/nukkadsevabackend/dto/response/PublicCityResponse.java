package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicCityResponse {

    private String cityName;
    private String state;
    private List<PincodeInfo> pincodes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PincodeInfo {
        private String pincode;
        private String areaName;
    }
}
