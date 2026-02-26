package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import java.util.List;

import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerOrderByCreatedAtDesc(Customers customer);

    List<Booking> findByProviderOrderByCreatedAtDesc(Provider provider);

    long countByCustomerIdAndStatusIn(Long customerId, List<BookingStatus> statuses);
}
