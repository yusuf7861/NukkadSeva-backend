package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {
    Optional<Customers> findByEmail(String email);

    @Query("""
        SELECT DISTINCT c
        FROM Customers c
        LEFT JOIN FETCH c.savedAddresses
        LEFT JOIN FETCH c.address
        LEFT JOIN FETCH c.user
        WHERE c.email = :email
    """)
    Optional<Customers> findWithAddressesByEmail(@Param("email") String email);
}
