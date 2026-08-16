package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.response.DashboardProviderDto;
import com.nukkadseva.nukkadsevabackend.dto.response.PublicCityResponse;
import com.nukkadseva.nukkadsevabackend.service.CityService;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/providers")
public class PublicController {

        private final ProviderService providerService;
        private final CityService cityService;

        @GetMapping
        public ResponseEntity<?> getProviders(
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String pincode,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "6") int limit) {
                Page<DashboardProviderDto> providerPage = providerService.searchProviders(category, city, pincode, page,
                                limit);

                Map<String, Object> response = new HashMap<>();
                response.put("providers", providerPage.getContent());
                response.put("pagination", Map.of(
                                "currentPage", providerPage.getNumber() + 1,
                                "totalPages", providerPage.getTotalPages(),
                                "totalItems", providerPage.getTotalElements()));

                return ResponseEntity.ok(response);
        }

        /**
         * Public endpoint to fetch active cities with their active pincodes.
         * Returns only essential fields: cityName, state, and pincodes (pincode,
         * areaName).
         */
        @GetMapping("/cities")
        public ResponseEntity<List<PublicCityResponse>> getActiveCitiesWithPincodes() {
                return ResponseEntity.ok(cityService.getActiveCitiesForPublic());
        }
}
