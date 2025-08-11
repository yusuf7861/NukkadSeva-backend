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
            return new ResponseEntity<>("Failed to process file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during registration: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Provider>> getPendingProviders() {
        List<Provider> pendingProviders = providerService.getPendingProviders();
        return new ResponseEntity<>(pendingProviders, HttpStatus.OK);
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
}

