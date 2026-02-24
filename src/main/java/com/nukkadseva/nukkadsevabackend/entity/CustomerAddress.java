package com.nukkadseva.nukkadsevabackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Table(name = "customer_address")
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customers customer;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // e.g., HOME, WORK, OTHER

    @Column(name = "flat_name", length = 200)
    private String flatName; // House No, Building, Flat

    @Column(name = "area", nullable = false, length = 200)
    private String area; // Area, Street, Sector

    @Column(name = "landmark", length = 200)
    private String landmark;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 20)
    private String pincode;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String buildFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (flatName != null && !flatName.trim().isEmpty())
            sb.append(flatName).append(", ");
        if (area != null && !area.trim().isEmpty())
            sb.append(area).append(", ");
        if (landmark != null && !landmark.trim().isEmpty())
            sb.append("Landmark: ").append(landmark).append(", ");
        if (city != null && !city.trim().isEmpty())
            sb.append(city).append(", ");
        if (state != null && !state.trim().isEmpty())
            sb.append(state).append(" - ");
        if (pincode != null && !pincode.trim().isEmpty())
            sb.append(pincode);
        return sb.toString().replaceAll(", $", "");
    }
}
