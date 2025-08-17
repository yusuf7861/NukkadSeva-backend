package com.nukkadseva.nukkadsevabackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
public class Customers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Column(name = "email", unique = true)
    private String email;

    // Store as PostgreSQL bytea, not OID/large object
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "photograph")
    private byte[] photograph;

    @Column(name = "photograph_content_type")
    private String photographContentType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    private Address address;

    // Inverse side of Users.customers association (Users owns FK via customer_id)
    @OneToOne(mappedBy = "customers")
    private Users user;
}