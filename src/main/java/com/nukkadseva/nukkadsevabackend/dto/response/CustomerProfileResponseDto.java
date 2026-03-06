package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerProfileResponseDto {
    private Long id;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String photograph;
    private AddressDto address;
    private List<CustomerAddressResponseDto> savedAddresses;
    private Long activeBookingsCount;
    private Long reviewsGivenCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
