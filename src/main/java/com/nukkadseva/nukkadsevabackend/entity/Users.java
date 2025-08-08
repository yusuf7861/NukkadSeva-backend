package com.nukkadseva.nukkadsevabackend.entity;

import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractAuditable;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customers customers;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "service_provider_id", referencedColumnName = "service_provider_id")
    private ServiceProviders serviceProviders;
}