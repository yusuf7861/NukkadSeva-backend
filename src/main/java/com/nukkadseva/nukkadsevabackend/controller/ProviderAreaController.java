package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.ProviderAreaRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderAreaResponse;
import com.nukkadseva.nukkadsevabackend.service.ProviderAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provider/areas")
@RequiredArgsConstructor
public class ProviderAreaController {

    private final ProviderAreaService providerAreaService;

    @PostMapping
    public ResponseEntity<ProviderAreaResponse> addArea(
            @Valid @RequestBody ProviderAreaRequest request,
            Authentication authentication) {
        return new ResponseEntity<>(providerAreaService.addArea(request, authentication), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProviderAreaResponse>> getMyAreas(Authentication authentication) {
        return ResponseEntity.ok(providerAreaService.getMyAreas(authentication));
    }

    @DeleteMapping("/{areaId}")
    public ResponseEntity<Void> removeArea(
            @PathVariable Long areaId,
            Authentication authentication) {
        providerAreaService.removeArea(areaId, authentication);
        return ResponseEntity.noContent().build();
    }
}
