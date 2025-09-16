package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Data;

@Data
public class DashboardProviderDto {
    private Long id;
    private String fullName;
    private String serviceCategory;
    private String profilePicture;
    private String serviceArea;
    private String businessName;
    private int experience;
    private String bio;
    private String availability;
    private String mobileNumber;
}