package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.ProviderAreaRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderAreaResponse;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.ProviderArea;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.repository.ProviderAreaRepository;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.service.ProviderAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderAreaServiceImpl implements ProviderAreaService {

    private final ProviderAreaRepository providerAreaRepository;
    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public ProviderAreaResponse addArea(ProviderAreaRequest request, Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        if (providerAreaRepository.existsByProviderAndCity(provider, request.getCity())) {
            throw new RuntimeException("Service area for this city already exists. Please update the existing one.");
        }

        ProviderArea newArea = ProviderArea.builder()
                .provider(provider)
                .city(request.getCity())
                .pincodes(request.getPincodes())
                .build();

        newArea = providerAreaRepository.save(newArea);

        return mapToResponse(newArea);
    }

    @Override
    public List<ProviderAreaResponse> getMyAreas(Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        return providerAreaRepository.findByProvider(provider).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeArea(Long areaId, Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        ProviderArea area = providerAreaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Provider area not found"));

        if (!area.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Not authorized to remove this area");
        }

        providerAreaRepository.delete(area);
    }

    private ProviderAreaResponse mapToResponse(ProviderArea area) {
        return ProviderAreaResponse.builder()
                .id(area.getId())
                .city(area.getCity())
                .pincodes(area.getPincodes())
                .createdAt(area.getCreatedAt())
                .build();
    }
}
