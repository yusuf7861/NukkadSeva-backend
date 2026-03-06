package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProviderProfileResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private LocalDate dob;
    private String profilePicture;
    private String bio;

    // Professional
    private String businessName;
    private String serviceCategory;
    private String serviceArea;
    private int experience;
    private String languages;
    private String availability;

    // Address
    private String fullAddress;
    private String city;
    private String state;
    private String pincode;

    // Status
    private String status;
    private Boolean isEmailVerified;
    private Boolean isApproved;
    private String rejectionReason;

    // Docs
    private String govtId;
    private String qualification;
    private String policeVerification;
    private String gstin;
}
