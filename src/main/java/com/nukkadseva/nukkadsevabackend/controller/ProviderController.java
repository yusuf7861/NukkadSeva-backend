package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.ProviderDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<?> registerProvider(@ModelAttribute ProviderDto providerDto) {
        try {
            Provider registeredProvider = providerService.registerProvider(providerDto);
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

    @GetMapping("/all")
    public ResponseEntity<List<Provider>> getAllProviders() {
        List<Provider> allProviders = providerService.getAllProviders();
        return new ResponseEntity<>(allProviders, HttpStatus.OK);
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
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            String email = null;
            if (principal instanceof com.nukkadseva.nukkadsevabackend.security.AuthUser authUser) {
                email = authUser.getEmail();
            } else if (principal instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
            } else if (principal != null) {
                email = principal.toString();
            }
            if (email == null || email.isBlank()) {
                return new ResponseEntity<>(Map.of("message", "Authenticated user email not found"), HttpStatus.UNAUTHORIZED);
            }
            Provider provider = providerService.getProviderByEmail(email);
            return new ResponseEntity<>(provider, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Failed to fetch profile: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
