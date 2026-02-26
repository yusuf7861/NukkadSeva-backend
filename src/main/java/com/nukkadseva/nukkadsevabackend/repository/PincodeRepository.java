package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.City;
import com.nukkadseva.nukkadsevabackend.entity.Pincode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PincodeRepository extends JpaRepository<Pincode, Long> {
    List<Pincode> findByCity(City city);

    List<Pincode> findByCityAndIsActiveTrue(City city);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.pincode FROM Provider p WHERE p.city = :city AND p.status = :status")
    List<String> findDistinctPincodeStringByCityAndProviderStatus(@org.springframework.data.repository.query.Param("city") String city, @org.springframework.data.repository.query.Param("status") com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus status);

    Optional<Pincode> findByPincodeAndCity(String pincode, City city);

    boolean existsByPincodeAndCity(String pincode, City city);
}

