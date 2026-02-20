package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.CityWithPincodesRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.CityWithPincodesResponse;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderDetailDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderSummaryDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import com.nukkadseva.nukkadsevabackend.service.CityService;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminController {

    private final ProviderService providerService;
    private final CityService cityService;

    @GetMapping("/providers/pending")
    public ResponseEntity<List<Provider>> getPendingProviders() {
        List<Provider> pendingProviders = providerService.getPendingProviders();
        return new ResponseEntity<>(pendingProviders, HttpStatus.OK);
    }

    @GetMapping("/providers/approved")
    public ResponseEntity<List<Provider>> getApprovedProviders() {
        List<Provider> approvedProviders = providerService.getProvidersByStatus(ProviderStatus.APPROVED);
        return new ResponseEntity<>(approvedProviders, HttpStatus.OK);
    }

    @GetMapping("/providers/rejected")
    public ResponseEntity<List<Provider>> getRejectedProviders() {
        List<Provider> rejectedProviders = providerService.getProvidersByStatus(ProviderStatus.REJECTED);
        return new ResponseEntity<>(rejectedProviders, HttpStatus.OK);
    }

    @PostMapping("/providers/{id}/approve")
    public ResponseEntity<?> approveProvider(@PathVariable Long id) {
        try {
            Provider approvedProvider = providerService.approveProvider(id);
            return new ResponseEntity<>(
                Map.of(
                    "success", true,
                    "message", "Provider approved successfully",
                    "providerId", approvedProvider.getId()
                ),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", "Failed to approve provider: " + e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/providers/{id}/reject")
    public ResponseEntity<?> rejectProvider(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        try {
            String reason = requestBody.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return new ResponseEntity<>(
                    Map.of(
                        "success", false,
                        "message", "Rejection reason is required"
                    ),
                    HttpStatus.BAD_REQUEST
                );
            }

            Provider rejectedProvider = providerService.rejectProvider(id, reason);
            return new ResponseEntity<>(
                Map.of(
                    "success", true,
                    "message", "Provider rejected",
                    "providerId", rejectedProvider.getId()
                ),
                HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", "Failed to reject provider: " + e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/all-providers")
    public ResponseEntity<List<ProviderSummaryDto>> getAllProvidersForAdmin() {
        List<ProviderSummaryDto> allProvidersForAdmin = providerService.getAllProvidersForAdmin();
        return ResponseEntity.ok(allProvidersForAdmin);
    }

    @GetMapping("/providers/{id}")
    public ResponseEntity<ProviderDetailDto> getProviderDetails(@PathVariable Long id) {
        ProviderDetailDto providerByIdForAdmin = providerService.getProviderByIdForAdmin(id);
        return ResponseEntity.ok(providerByIdForAdmin);
    }

    // ============ City Management Endpoints ============

    @PostMapping("/cities")
    public ResponseEntity<?> addCityWithPincodes(@Valid @RequestBody CityWithPincodesRequest request) {
        try {
            CityWithPincodesResponse response = cityService.addCityWithPincodes(request);
            return new ResponseEntity<>(
                Map.of(
                    "success", true,
                    "message", "City and pincodes added successfully",
                    "data", response
                ),
                HttpStatus.CREATED
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", e.getMessage()
                ),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", "Failed to add city: " + e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityWithPincodesResponse>> getAllCities() {
        List<CityWithPincodesResponse> cities = cityService.getAllCitiesWithPincodes();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/cities/{cityId}")
    public ResponseEntity<?> getCityById(@PathVariable Long cityId) {
        try {
            CityWithPincodesResponse city = cityService.getCityWithPincodesById(cityId);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", e.getMessage()
                ),
                HttpStatus.NOT_FOUND
            );
        }
    }

    @PostMapping("/cities/{cityId}/pincodes")
    public ResponseEntity<?> addPincodesToCity(
            @PathVariable Long cityId,
            @RequestBody List<CityWithPincodesRequest.PincodeRequest> pincodes) {
        try {
            CityWithPincodesResponse response = cityService.addPincodesToCity(cityId, pincodes);
            return new ResponseEntity<>(
                Map.of(
                    "success", true,
                    "message", "Pincodes added successfully",
                    "data", response
                ),
                HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", e.getMessage()
                ),
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @PatchMapping("/cities/{cityId}/status")
    public ResponseEntity<?> toggleCityStatus(
            @PathVariable Long cityId,
            @RequestBody Map<String, Boolean> requestBody) {
        try {
            Boolean isActive = requestBody.get("isActive");
            if (isActive == null) {
                return new ResponseEntity<>(
                    Map.of(
                        "success", false,
                        "message", "isActive field is required"
                    ),
                    HttpStatus.BAD_REQUEST
                );
            }

            CityWithPincodesResponse response = cityService.toggleCityStatus(cityId, isActive);
            return new ResponseEntity<>(
                Map.of(
                    "success", true,
                    "message", "City status updated successfully",
                    "data", response
                ),
                HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", e.getMessage()
                ),
                HttpStatus.NOT_FOUND
            );
        }
    }

    @DeleteMapping("/cities/{cityId}")
    public ResponseEntity<?> deleteCity(@PathVariable Long cityId) {
        try {
            cityService.deleteCity(cityId);
            return new ResponseEntity<>(
                Map.of(
                    "success", true,
                    "message", "City deleted successfully"
                ),
                HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                Map.of(
                    "success", false,
                    "message", e.getMessage()
                ),
                HttpStatus.NOT_FOUND
            );
        }
    }
}
