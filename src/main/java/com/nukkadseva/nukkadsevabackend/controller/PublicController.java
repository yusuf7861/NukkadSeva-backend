package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.PublicProviderDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/providers")
public class PublicController {

    private final ProviderService providerService;

    @GetMapping
    public ResponseEntity<?> getProviders(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit
    ) {
        Page<Provider> providerPage = providerService.searchProviders(category, city, pincode, page, limit);

        // Filter providers to only include those with "approved" status
        List<Provider> approvedProviders = providerPage.getContent().stream()
                .filter(provider -> "approved".equalsIgnoreCase(provider.getStatus().name()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("providers", approvedProviders);
        response.put("pagination", Map.of(
                "currentPage", providerPage.getNumber() + 1,
                "totalPages", providerPage.getTotalPages(),
                "totalItems", approvedProviders.size()
        ));

        return ResponseEntity.ok(response);
    }
}
