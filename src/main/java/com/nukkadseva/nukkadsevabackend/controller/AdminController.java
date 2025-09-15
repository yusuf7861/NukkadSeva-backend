package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.response.ProviderSummaryDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
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
}
