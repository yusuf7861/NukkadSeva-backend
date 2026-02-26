package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.CustomerAddress;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomer(Customers customer);

    Optional<CustomerAddress> findByIdAndCustomer(Long id, Customers customer);

    Optional<CustomerAddress> findByCustomerAndIsDefaultTrue(Customers customer);
}
