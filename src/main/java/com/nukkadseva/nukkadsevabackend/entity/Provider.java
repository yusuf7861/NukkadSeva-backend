package com.nukkadseva.nukkadsevabackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "provider")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id", nullable = false)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "mobile_number", nullable = false, length = 15, unique = true)
    private String mobileNumber;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "photograph", length = 500)
    private String photograph;

    @Column(name = "business_name", length = 150)
    private String businessName;

    @Column(name = "service_category", nullable = false, length = 100)
    private String serviceCategory;

    @Column(name = "service_area", nullable = false, length = 100)
    private String serviceArea;

    @Column(name = "experience", nullable = false)
    private int experience;

    @Column(name = "languages", nullable = false, length = 200)
    private String languages;

    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;

    @Column(name = "state", nullable = false, length = 50)
    private String state;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "govt_id", length = 500)
    private String govtId;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "qualification", length = 500)
    private String qualification;

    @Column(name = "police_verification", length = 500)
    private String policeVerification;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;

    @Column(name = "availability", nullable = false, length = 100)
    private String availability;

    @Column(name = "agree_to_s", nullable = false)
    private boolean agreeToS;

    @Column(name = "agree_to_bg_check", nullable = false)
    private boolean agreeToBgCheck;

    @Column(name = "is_email_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isEmailVerified = false;

    @Column(name = "is_approved", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isApproved = false;

    @OneToOne(mappedBy = "provider")
    private Users user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String status = "PENDING"; // PENDING → VERIFIED → APPROVED/REJECTED

    @Column(name = "verification_token", length = 500)
    private String verificationToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
}
