package com.nukkadseva.nukkadsevabackend.entity;

import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.PaymentMethod;
import com.nukkadseva.nukkadsevabackend.entity.enums.PaymentStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.Services;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@DynamicUpdate
@Entity
@Table(
        name = "booking",
        indexes = {
                @Index(name = "idx_booking_customer", columnList = "customer_id"),
                @Index(name = "idx_booking_provider", columnList = "provider_id"),
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_date", columnList = "booking_date_time"),
                @Index(name = "idx_booking_review", columnList = "review_id")
        }
)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customers customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private Services serviceType;

    @Column(name = "booking_date_time", nullable = false)
    private LocalDateTime bookingDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
        private LocalDateTime updatedAt;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "estimate_price")
        private BigDecimal priceEstimate;
    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "note", columnDefinition = "TEXT")
        private String note;
    @Column(name = "provider_notes", columnDefinition = "TEXT")
    private String providerNote;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id", unique = true)
    private Review review;
}