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

    Optional<Pincode> findByPincodeAndCity(String pincode, City city);

    boolean existsByPincodeAndCity(String pincode, City city);
}

