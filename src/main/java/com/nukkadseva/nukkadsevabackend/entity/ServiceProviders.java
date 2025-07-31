package com.nukkadseva.nukkadsevabackend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "service_providers")
@EntityListeners(AuditingEntityListener.class)
public class ServiceProviders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "mobile_number", nullable = false, unique = true)
    private String mobileNumber;

    @Column(name = "email", unique = true)
    private String email;

    @Lob
    @Column(name = "photograph", columnDefinition = "bytea")
    private byte[] photograph;

    @Column(name = "photograph_content_type")
    private String photographContentType;

    @Column(name = "business_name")
    private String businessName;

    @ElementCollection
    @CollectionTable(name = "service_categories", joinColumns = @JoinColumn(name = "service_provider_id"))
    @Column(name = "category")
    private List<String> serviceCategory;

    @ElementCollection
    @CollectionTable(name = "service_areas", joinColumns = @JoinColumn(name = "service_provider_id"))
    @Column(name = "area")
    private List<String> serviceArea;

    @Column(name = "experience", nullable = false)
    private Integer experience;

    @Column(name = "languages", nullable = false)
    private String languages;

    @Lob
    @Column(name = "govt_id", columnDefinition = "bytea", nullable = false)
    private byte[] govtId;

    @Column(name = "govt_id_content_type", nullable = false)
    private String govtIdContentType;

    @Lob
    @Column(name = "selfie", columnDefinition = "bytea", nullable = false)
    private byte[] selfie;

    @Column(name = "selfie_content_type")
    private String selfieContentType;

    @Column(name = "gstin")
    private String gstin;

    @Lob
    @Column(name = "qualification", columnDefinition = "bytea")
    private byte[] qualification;

    @Column(name = "qualification_content_type")
    private String qualificationContentType;

    @Lob
    @Column(name = "police_verification", columnDefinition = "bytea")
    private byte[] policeVerification;

    @Column(name = "police_verification_content_type")
    private String policeVerificationContentType;

    @Column(name = "bio", length = 2000)
    private String bio;

    @Lob
    @Column(name = "profile_picture", columnDefinition = "bytea")
    private byte[] profilePicture;

    @Column(name = "profile_picture_content_type")
    private String profilePictureContentType;

    @Column(name = "availability", nullable = false)
    private String availability;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id", nullable = false)
    private Address address;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}