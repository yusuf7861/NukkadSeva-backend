package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider.id = :providerId")
    Double getAverageRatingForProvider(@Param("providerId") Long providerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.customer.id = :customerId")
    Double getAverageRatingForCustomer(@Param("customerId") Long customerId);

    boolean existsByBookingId(UUID bookingId);

    List<Review> findByProviderIdOrderByCreatedAtDesc(Long providerId);
}
