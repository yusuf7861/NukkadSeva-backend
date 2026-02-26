package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByCityName(String cityName);

    List<City> findByIsActiveTrue();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.city FROM Provider p WHERE p.status = :status")
    List<String> findDistinctCityNameByProviderStatus(@org.springframework.data.repository.query.Param("status") com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus status);

    boolean existsByCityName(String cityName);
}

