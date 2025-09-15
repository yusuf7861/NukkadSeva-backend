package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long>, JpaSpecificationExecutor<Provider> {
    List<Provider> findByStatus(ProviderStatus status);
    List<Provider> findByStatusAndIsEmailVerified(String status, Boolean isEmailVerified);
    Optional<Provider> findByVerificationToken(String verificationToken);
    Optional<Provider> findByEmail(String email);
    Optional<Provider> findByMobileNumber(String mobileNumber);
}
