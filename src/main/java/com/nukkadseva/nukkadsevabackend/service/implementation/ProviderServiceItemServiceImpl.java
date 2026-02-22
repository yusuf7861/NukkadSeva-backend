package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.ServiceDto;
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

@Service
@RequiredArgsConstructor
public class ProviderServiceItemServiceImpl implements ProviderServiceItemService {

    private final ProviderServiceItemRepository serviceItemRepository;
    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public ProviderServiceItem createService(ServiceDto serviceDto, Authentication authentication) {
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
                .pincodes(serviceDto.getPincodes())
                .build();

        return serviceItemRepository.save(newItem);
    }

    @Override
    public List<ProviderServiceItem> getMyServices(Authentication authentication) {
        String email = authentication.getName();
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found for email: " + email));

        return serviceItemRepository.findByProvider(provider);
    }

    @Override
    public List<ProviderServiceItem> searchServices(String city, String pincode, Long providerId) {
        return serviceItemRepository.searchServices(city, pincode, providerId);
    }

    @Override
    @Transactional
    public ProviderServiceItem toggleServiceStatus(Long serviceId, Authentication authentication) {
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
        return serviceItemRepository.save(service);
    }
}
