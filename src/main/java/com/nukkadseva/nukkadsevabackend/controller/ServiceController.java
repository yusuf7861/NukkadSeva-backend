package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.ServiceDto;
import com.nukkadseva.nukkadsevabackend.security.AuthUser;
import com.nukkadseva.nukkadsevabackend.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping("/services")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ServiceDto> createService(@RequestBody ServiceDto serviceDto) {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ServiceDto createdService = serviceService.createService(serviceDto, authUser.getEmail());
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @GetMapping("/provider/services")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<List<ServiceDto>> getProviderServices() {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(serviceService.getServicesByProvider(authUser.getEmail()));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @GetMapping("/services/category/{category}")
    public ResponseEntity<List<ServiceDto>> getServicesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(serviceService.getServicesByCategory(category));
    }

    @GetMapping("/services/search")
    public ResponseEntity<List<ServiceDto>> searchServices(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode) {
        return ResponseEntity.ok(serviceService.searchServices(city, pincode));
    }
}
