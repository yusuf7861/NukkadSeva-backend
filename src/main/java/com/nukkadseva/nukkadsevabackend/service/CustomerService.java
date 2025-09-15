package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.entity.Customers;

public interface CustomerService {
    void customerRegistration(UserRequest userRequest);
    Customers updateCustomerProfile(CustomerProfileUpdateRequest request, String email);
    Customers getCustomerProfile(String name);
}
