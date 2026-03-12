package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.ProviderDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderProfileResponseDto;
import com.nukkadseva.nukkadsevabackend.service.DashboardService;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    private final DashboardService dashboardService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<?> registerProvider(@ModelAttribute ProviderDto providerDto) {
        try {
            ProviderProfileResponseDto registeredProvider = providerService.registerProvider(providerDto);
            return new ResponseEntity<>(registeredProvider, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(
                    Map.of("message", "Failed to process file: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    Map.of("message", e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("message", "An error occurred during registration: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyProviderEmail(@RequestParam String token) {
        boolean verified = providerService.verifyProviderEmail(token);
        if (verified) {
            return new ResponseEntity<>("Email verified successfully. Your application is now pending admin approval.",
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid or expired verification token.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String email = authentication.getName();
            com.nukkadseva.nukkadsevabackend.dto.response.ProviderProfileResponseDto provider = providerService
                    .getProviderByEmail(email);
            return new ResponseEntity<>(provider, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Failed to fetch profile: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<com.nukkadseva.nukkadsevabackend.dto.response.ProviderDashboardDto> getDashboard(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getProviderDashboard(authentication));
    }
}
