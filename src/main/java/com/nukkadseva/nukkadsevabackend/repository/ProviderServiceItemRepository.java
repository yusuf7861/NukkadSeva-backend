package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.ProviderServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderServiceItemRepository extends JpaRepository<ProviderServiceItem, Long> {
        List<ProviderServiceItem> findByProvider(Provider provider);

        @Query("SELECT DISTINCT s FROM ProviderServiceItem s JOIN s.provider p LEFT JOIN p.providerAreas a "
                        +
                        "WHERE p.status = com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus.APPROVED AND s.isActive = true " +
                        "AND (:city IS NULL OR a.city = :city OR p.city = :city) " +
                        "AND (:pincode IS NULL OR p.serviceArea LIKE CONCAT('%', :pincode, '%') OR :pincode MEMBER OF a.pincodes) " +
                        "AND (:providerId IS NULL OR p.id = :providerId)")
        List<ProviderServiceItem> searchServices(
                        @Param("city") String city,
                        @Param("pincode") String pincode,
                        @Param("providerId") Long providerId);
}
