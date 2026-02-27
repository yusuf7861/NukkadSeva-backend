package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto;
import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.CustomerRegistrationRequest;
import com.nukkadseva.nukkadsevabackend.entity.Customers;

import java.util.List;

public interface CustomerService {
    void customerRegistration(CustomerRegistrationRequest request);

    Customers updateCustomerProfile(CustomerProfileUpdateRequest request, String email);

    // Address Management
    CustomerAddressDto addAddress(String email, CustomerAddressDto addressDto);

    CustomerAddressDto updateAddress(String email, Long addressId,
            CustomerAddressDto addressDto);

    void deleteAddress(String email, Long addressId);

    void setDefaultAddress(String email, Long addressId);

    List<CustomerAddressDto> getSavedAddresses(String email);

    Customers getCustomerProfile(String name);
}
