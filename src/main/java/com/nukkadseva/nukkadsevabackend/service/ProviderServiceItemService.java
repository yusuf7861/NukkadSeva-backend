package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.ServiceDto;
import com.nukkadseva.nukkadsevabackend.entity.ProviderServiceItem;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProviderServiceItemService {
    ProviderServiceItem createService(ServiceDto serviceDto, Authentication authentication);

    List<ProviderServiceItem> getMyServices(Authentication authentication);

    List<ProviderServiceItem> searchServices(String city, String pincode, Long providerId);

    ProviderServiceItem toggleServiceStatus(Long serviceId, Authentication authentication);
}
