package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.ProviderDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<?> registerProvider(@ModelAttribute ProviderDto providerDto) {
        try {
            Provider registeredProvider = providerService.registerProvider(providerDto);
            return new ResponseEntity<>(registeredProvider, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(
                Map.of("message", "Failed to process file: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                Map.of("message", e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "An error occurred during registration: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Provider>> getPendingProviders() {
        List<Provider> pendingProviders = providerService.getPendingProviders();
        return new ResponseEntity<>(pendingProviders, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Provider>> getAllProviders() {
        List<Provider> allProviders = providerService.getAllProviders();
        return new ResponseEntity<>(allProviders, HttpStatus.OK);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Provider> approveProvider(@PathVariable Long id) {
        Provider approvedProvider = providerService.approveProvider(id);
        return new ResponseEntity<>(approvedProvider, HttpStatus.OK);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Provider> rejectProvider(@PathVariable Long id) {
        Provider rejectedProvider = providerService.rejectProvider(id);
        return new ResponseEntity<>(rejectedProvider, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable Long id) {
        return providerService.getProviderById(id)
                .map(provider -> new ResponseEntity<>(provider, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyProviderEmail(@RequestParam String token) {
        boolean verified = providerService.verifyProviderEmail(token);
        if (verified) {
            return new ResponseEntity<>("Email verified successfully. Your application is now pending admin approval.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid or expired verification token.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/verified")
    public ResponseEntity<List<Provider>> getVerifiedProviders() {
        List<Provider> verifiedProviders = providerService.getProvidersByStatus("VERIFIED");
        return new ResponseEntity<>(verifiedProviders, HttpStatus.OK);
    }
}
