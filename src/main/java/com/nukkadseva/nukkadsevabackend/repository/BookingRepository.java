package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Booking;
import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByProviderIdAndStatusOrderByCreatedAtDesc(Long providerId, BookingStatus status);

    List<Booking> findByProviderIdOrderByCreatedAtDesc(Long providerId);
}
