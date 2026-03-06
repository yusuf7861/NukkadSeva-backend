package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.ServiceDto;
import com.nukkadseva.nukkadsevabackend.entity.ProviderServiceItem;
import org.springframework.security.core.Authentication;

import java.util.List;

import com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto;

public interface ProviderServiceItemService {
    ProviderServiceItemResponseDto createService(ServiceDto serviceDto, Authentication authentication);

    List<ProviderServiceItemResponseDto> getMyServices(Authentication authentication);

    List<ProviderServiceItem> searchServices(String city, String pincode, Long providerId);

    ProviderServiceItemResponseDto toggleServiceStatus(Long serviceId, Authentication authentication);
}
