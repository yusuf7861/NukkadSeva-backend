package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT p.city FROM Provider p WHERE p.status = :status")
    List<String> findDistinctCityByStatus(@Param("status") ProviderStatus status);

    @Query("SELECT DISTINCT p.pincode FROM Provider p WHERE p.city = :city AND p.status = :status")
    List<String> findDistinctPincodeByCityAndStatus(@Param("city") String city, @Param("status") ProviderStatus status);
}
