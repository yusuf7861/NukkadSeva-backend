package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.ServiceDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.ProviderService;
import com.nukkadseva.nukkadsevabackend.exception.ResourceNotFoundException;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.ServiceRepository;
import com.nukkadseva.nukkadsevabackend.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;

    @Override
    public ServiceDto createService(ServiceDto serviceDto, String providerEmail) {
        Provider provider = providerRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with email: " + providerEmail));

        ProviderService service = new ProviderService();
        service.setName(serviceDto.getName());
        service.setDescription(serviceDto.getDescription());
        service.setCategory(serviceDto.getCategory());
        service.setPrice(serviceDto.getPrice());
        service.setDurationMinutes(serviceDto.getDurationMinutes());
        service.setProvider(provider);
        service.setActive(true);
        if (serviceDto.getPincodes() != null) {
            service.setPincodes(serviceDto.getPincodes());
        }

        ProviderService savedService = serviceRepository.save(service);
        return mapToDto(savedService);
    }

    @Override
    public List<ServiceDto> getAllServices() {
        return serviceRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDto> getServicesByProvider(String providerEmail) {
        Provider provider = providerRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with email: " + providerEmail));

        return serviceRepository.findByProviderId(provider.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDto> getServicesByCategory(String category) {
        return serviceRepository.findByCategory(category).stream()
                .filter(ProviderService::isActive)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceDto getServiceById(Long id) {
        ProviderService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return mapToDto(service);
    }

    @Override
    public List<ServiceDto> searchServices(String city, String pincode) {
        return serviceRepository.findByIsActiveTrue().stream()
                .filter(service -> {
                    boolean matchesCity = city == null || city.isEmpty() ||
                            service.getProvider().getCity().equalsIgnoreCase(city);
                    boolean matchesPincode = pincode == null || pincode.isEmpty() ||
                            service.getPincodes().contains(pincode);
                    return matchesCity && matchesPincode;
                })
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ServiceDto mapToDto(ProviderService service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setCategory(service.getCategory());
        dto.setPrice(service.getPrice());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setActive(service.isActive());
        dto.setProviderId(service.getProvider().getId());
        dto.setProviderName(service.getProvider().getFullName());
        dto.setPincodes(service.getPincodes());
        dto.setProviderVerified(service.getProvider().getIsApproved()); // Assuming isApproved means verified for now,
                                                                        // or check isEmailVerified? User asked for
                                                                        // "verified badge", usually implies identity
                                                                        // verification. Using isApproved (admin
                                                                        // approved) is safer.
        return dto;
    }
}
