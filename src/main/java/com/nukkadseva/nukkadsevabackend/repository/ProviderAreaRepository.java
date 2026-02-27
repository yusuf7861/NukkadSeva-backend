package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.ProviderArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderAreaRepository extends JpaRepository<ProviderArea, Long> {
    List<ProviderArea> findByProvider(Provider provider);

    boolean existsByProviderAndCity(Provider provider, String city);
}
