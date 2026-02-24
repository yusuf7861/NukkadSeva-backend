package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.entity.Customers;

public interface CustomerService {
    void customerRegistration(UserRequest userRequest);

    Customers updateCustomerProfile(CustomerProfileUpdateRequest request, String email);

    // Address Management
    com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto addAddress(String email,
            com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto addressDto);

    com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto updateAddress(String email, Long addressId,
            com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto addressDto);

    void deleteAddress(String email, Long addressId);

    void setDefaultAddress(String email, Long addressId);

    java.util.List<com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto> getSavedAddresses(String email);

    Customers getCustomerProfile(String name);
}
