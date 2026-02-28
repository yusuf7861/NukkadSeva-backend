package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.ProviderAreaRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderAreaResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProviderAreaService {
    ProviderAreaResponse addArea(ProviderAreaRequest request, Authentication authentication);

    List<ProviderAreaResponse> getMyAreas(Authentication authentication);

    void removeArea(Long areaId, Authentication authentication);
}
