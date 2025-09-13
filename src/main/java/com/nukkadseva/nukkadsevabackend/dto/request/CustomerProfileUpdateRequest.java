package com.nukkadseva.nukkadsevabackend.dto.request;

import lombok.Data;

@Data
public class CustomerProfileUpdateRequest {
    private String name;
    private String phone;
    private String fullAddress;
    private String state;
    private String city;
    private String pincode;
}
