package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long>, JpaSpecificationExecutor<Provider> {
    List<Provider> findByStatus(String status);
    List<Provider> findByStatusAndIsEmailVerified(String status, Boolean isEmailVerified);
    Optional<Provider> findByVerificationToken(String verificationToken);
    Optional<Provider> findByEmail(String email);
    Optional<Provider> findByMobileNumber(String mobileNumber);
}
