package com.nukkadseva.nukkadsevabackend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data
public class ProviderDto {
    private String fullName;
    private LocalDate dob;
    private String mobileNumber;
    private String email;
    private MultipartFile photograph;
    private String businessName;
    private String serviceCategory;
    private String serviceArea;
    private int experience;
    private String languages;
    private String fullAddress;
    private String state;
    private String city;
    private String pincode;

    private MultipartFile govtId;

    private String gstin;
    private MultipartFile qualification;
    private MultipartFile policeVerification;
    private String bio;
    private MultipartFile profilePicture;
    private String availability;
    private boolean agreeToS;
    private boolean agreeToBgCheck;
}
