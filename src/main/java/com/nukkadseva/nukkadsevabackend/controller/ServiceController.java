package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.ServiceDto;
import com.nukkadseva.nukkadsevabackend.entity.ProviderServiceItem;
import com.nukkadseva.nukkadsevabackend.service.ProviderServiceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Builder;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ProviderServiceItemService serviceItemService;

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PostMapping
    public ResponseEntity<com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto> createService(
            @Valid @RequestBody ServiceDto serviceDto,
            Authentication authentication) {
        com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto createdService = serviceItemService
                .createService(serviceDto, authentication);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/me")
    public ResponseEntity<List<com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto>> getMyServices(
            Authentication authentication) {
        List<com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto> myServices = serviceItemService
                .getMyServices(authentication);
        return ResponseEntity.ok(myServices);
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto> toggleServiceStatus(
            @PathVariable Long id,
            Authentication authentication) {
        com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto updatedService = serviceItemService
                .toggleServiceStatus(id, authentication);
        return ResponseEntity.ok(updatedService);
    }

    @Data
    @Builder
    public static class ServiceSearchResultDto {
        private Long id;
        private String name;
        private String description;
        private String category;
        private BigDecimal price;
        private Integer durationMinutes;
        private String providerName;
        private Long providerId;
        private List<String> pincodes;
        private boolean providerVerified;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceSearchResultDto>> searchServices(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) Long providerId) {

        List<ProviderServiceItem> services = serviceItemService.searchServices(city, pincode, providerId);

        List<ServiceSearchResultDto> results = services.stream()
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

        return ResponseEntity.ok(results);
    }
}
