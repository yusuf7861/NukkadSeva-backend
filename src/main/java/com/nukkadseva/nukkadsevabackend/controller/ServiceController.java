package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.ServiceDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderServiceItemResponseDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ServiceSearchResultDto;
import com.nukkadseva.nukkadsevabackend.service.ProviderServiceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ProviderServiceItemService serviceItemService;

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PostMapping
    public ResponseEntity<ProviderServiceItemResponseDto> createService(
            @Valid @RequestBody ServiceDto serviceDto,
            Authentication authentication) {
        ProviderServiceItemResponseDto createdService = serviceItemService
                .createService(serviceDto, authentication);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/me")
    public ResponseEntity<List<ProviderServiceItemResponseDto>> getMyServices(
            Authentication authentication) {
        List<ProviderServiceItemResponseDto> myServices = serviceItemService
                .getMyServices(authentication);
        return ResponseEntity.ok(myServices);
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ProviderServiceItemResponseDto> toggleServiceStatus(
            @PathVariable Long id,
            Authentication authentication) {
        ProviderServiceItemResponseDto updatedService = serviceItemService
                .toggleServiceStatus(id, authentication);
        return ResponseEntity.ok(updatedService);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceSearchResultDto>> searchServices(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) Long providerId) {

        List<ServiceSearchResultDto> results = serviceItemService.searchServices(city, pincode, providerId);
        return ResponseEntity.ok(results);
    }
}
