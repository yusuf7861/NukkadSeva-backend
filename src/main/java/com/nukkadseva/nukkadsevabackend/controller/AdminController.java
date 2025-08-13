package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private ProviderService providerService;

    @GetMapping("/providers/pending")
    public ResponseEntity<List<Provider>> getPendingProviders() {
        List<Provider> pendingProviders = providerService.getPendingProviders();
        return new ResponseEntity<>(pendingProviders, HttpStatus.OK);
    }

    @GetMapping("/providers/approved")
    public ResponseEntity<List<Provider>> getApprovedProviders() {
        List<Provider> approvedProviders = providerService.getProvidersByStatus("APPROVED");
        return new ResponseEntity<>(approvedProviders, HttpStatus.OK);
    }

    @GetMapping("/providers/rejected")
    public ResponseEntity<List<Provider>> getRejectedProviders() {
        List<Provider> rejectedProviders = providerService.getProvidersByStatus("REJECTED");
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

//    @GetMapping("/providers/{id}")
//    public ResponseEntity<?> getProviderDetails(@PathVariable Long id) {
//        return providerService.getProviderById(id)
//                .map(provider -> new ResponseEntity<>(provider, HttpStatus.OK))
//                .orElse(new ResponseEntity<>(
//                    Map.of("message", "Provider not found"),
//                    HttpStatus.NOT_FOUND
//                ));
//    }
}
