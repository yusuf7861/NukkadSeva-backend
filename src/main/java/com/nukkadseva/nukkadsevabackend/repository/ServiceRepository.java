package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ProviderService, Long> {
    List<ProviderService> findByProviderId(Long providerId);

    List<ProviderService> findByCategory(String category);

    List<ProviderService> findByIsActiveTrue();
}
