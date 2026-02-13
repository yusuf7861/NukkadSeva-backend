package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.ServiceDto;

import java.util.List;

public interface ServiceService {
    ServiceDto createService(ServiceDto serviceDto, String providerEmail);

    List<ServiceDto> getAllServices();

    List<ServiceDto> getServicesByProvider(String providerEmail);

    List<ServiceDto> getServicesByCategory(String category);

    ServiceDto getServiceById(Long id);

    List<ServiceDto> searchServices(String city, String pincode);
}
