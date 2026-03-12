package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.ServiceDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ServiceSearchResultDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.ProviderServiceItem;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.ProviderServiceItemRepository;
import com.nukkadseva.nukkadsevabackend.service.ProviderServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderServiceItemServiceImpl implements ProviderServiceItemService {

    private final ProviderServiceItemRepository serviceItemRepository;
    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public ProviderServiceItemResponseDto createService(ServiceDto serviceDto, Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        ProviderServiceItem newItem = ProviderServiceItem.builder()
                .provider(provider)
                .name(serviceDto.getName())
                .description(serviceDto.getDescription())
                .category(serviceDto.getCategory())
                .price(serviceDto.getPrice())
                .durationMinutes(serviceDto.getDurationMinutes())
                .isActive(serviceDto.isActive())
                .build();

        ProviderServiceItem savedItem = serviceItemRepository.save(newItem);
        return mapToDto(savedItem);
    }

    @Override
    public List<ProviderServiceItemResponseDto> getMyServices(Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        List<ProviderServiceItem> items = serviceItemRepository.findByProvider(provider);
        return items.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceSearchResultDto> searchServices(String city, String pincode, Long providerId) {
        List<ProviderServiceItem> services = serviceItemRepository.searchServices(city, pincode, providerId);

        return services.stream()
                .map((ProviderServiceItem service) -> ServiceSearchResultDto.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .description(service.getDescription())
                        .category(service.getCategory())
                        .price(service.getPrice())
                        .durationMinutes(service.getDurationMinutes())
                        .providerName(service.getProvider().getFullName())
                        .providerId(service.getProvider().getId())
                        .pincodes(service.getProvider().getProviderAreas().stream()
                                .flatMap(area -> area.getPincodes().stream()).collect(Collectors.toList()))
                        .providerVerified(Boolean.TRUE.equals(service.getProvider().getIsApproved()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProviderServiceItemResponseDto toggleServiceStatus(Long serviceId, Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        ProviderServiceItem service = serviceItemRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + serviceId));

        // Verify ownership
        if (!service.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You are not authorized to modify this service");
        }

        service.setActive(!service.isActive());
        ProviderServiceItem savedItem = serviceItemRepository.save(service);
        return mapToDto(savedItem);
    }

    private ProviderServiceItemResponseDto mapToDto(ProviderServiceItem item) {
        return ProviderServiceItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .price(item.getPrice())
                .durationMinutes(item.getDurationMinutes())
                .isActive(item.isActive())
                .build();
    }
}
