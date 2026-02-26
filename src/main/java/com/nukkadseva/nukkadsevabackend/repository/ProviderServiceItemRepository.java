package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.ProviderServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderServiceItemRepository extends JpaRepository<ProviderServiceItem, Long> {
        List<ProviderServiceItem> findByProvider(Provider provider);

        @org.springframework.data.jpa.repository.Query("SELECT s FROM ProviderServiceItem s JOIN s.provider p " +
                        "WHERE p.status = 'APPROVED' AND s.isActive = true " +
                        "AND (:city IS NULL OR p.city = :city) " +
                        "AND (:pincode IS NULL OR p.serviceArea LIKE %:pincode% OR :pincode IN elements(s.pincodes)) " +
                        "AND (:providerId IS NULL OR p.id = :providerId)")
        List<ProviderServiceItem> searchServices(
                        @org.springframework.data.repository.query.Param("city") String city,
                        @org.springframework.data.repository.query.Param("pincode") String pincode,
                        @org.springframework.data.repository.query.Param("providerId") Long providerId);
}
