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
    private String city;
    private String state;
    private Double averageRating;
    private Integer reviewCount;
    // Trust signals
    private Boolean isVerified;
    private Integer jobsCompleted;
    private Integer responseTimeMinutes;
    private String serviceGuarantees;
    private String memberSince;
}