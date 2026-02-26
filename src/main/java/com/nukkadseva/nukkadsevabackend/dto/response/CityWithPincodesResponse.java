package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityWithPincodesResponse {

    private Long id;
    private String cityName;
    private String state;
    private Boolean isActive;
    private List<PincodeDto> pincodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PincodeDto {
        private Long id;
        private String pincode;
        private String areaName;
        private Boolean isActive;
    }
}

