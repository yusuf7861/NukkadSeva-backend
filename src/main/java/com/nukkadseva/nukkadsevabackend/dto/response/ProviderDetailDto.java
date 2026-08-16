package com.nukkadseva.nukkadsevabackend.dto.response;

import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProviderDetailDto {
    private Long id;
    private String fullName;
    private LocalDate dob;
    private String mobileNumber;
    private String email;
    private String photograph;
    private String businessName;
    private String serviceCategory;
    private String serviceArea;
    private int experience;
    private String languages;
    private String fullAddress;
    private String state;
    private String city;
    private String pincode;
    private String govtId;
    private String gstin;
    private String qualification;
    private String policeVerification;
    private String bio;
    private String profilePicture;
    private String availability;
    private boolean agreeToS;
    private boolean agreeToBgCheck;
    private Boolean isEmailVerified;
    private Boolean isApproved;
    private ProviderStatus status;
    private LocalDateTime createdAt;
    private String rejectionReason;
}
