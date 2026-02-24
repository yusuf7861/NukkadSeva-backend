package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.service.CustomerService;
import com.nukkadseva.nukkadsevabackend.security.AuthUser;
import com.nukkadseva.nukkadsevabackend.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("/**")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final com.nukkadseva.nukkadsevabackend.service.DashboardService dashboardService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> customerRegistration(@Valid @RequestBody UserRequest userRequest) {
        customerService.customerRegistration(userRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("httpStatusCode", HttpStatus.CREATED.value());
        response.put("message", "User registered successfully.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Customers> getCustomerProfile(@CurrentUser AuthUser user) {
        return ResponseEntity.ok(customerService.getCustomerProfile(user.getEmail()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCustomerProfile(@RequestBody CustomerProfileUpdateRequest request,
            @CurrentUser AuthUser user) {
        customerService.updateCustomerProfile(request, user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<com.nukkadseva.nukkadsevabackend.dto.response.CustomerDashboardDto> getDashboard(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getCustomerDashboard(authentication));
    }

    // --- Address Management Endpoints ---

    @GetMapping("/address")
    public ResponseEntity<List<CustomerAddressDto>> getAddresses(@CurrentUser AuthUser user) {
        return ResponseEntity.ok(customerService.getSavedAddresses(user.getEmail()));
    }

    @PostMapping("/address")
    public ResponseEntity<CustomerAddressDto> addAddress(@Valid @RequestBody CustomerAddressDto addressDto,
            @CurrentUser AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addAddress(user.getEmail(), addressDto));
    }

    @PutMapping("/address/{id}")
    public ResponseEntity<CustomerAddressDto> updateAddress(@PathVariable Long id,
            @Valid @RequestBody CustomerAddressDto addressDto, @CurrentUser AuthUser user) {
        return ResponseEntity.ok(customerService.updateAddress(user.getEmail(), id, addressDto));
    }

    @DeleteMapping("/address/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, @CurrentUser AuthUser user) {
        customerService.deleteAddress(user.getEmail(), id);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }

    @PutMapping("/address/{id}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id, @CurrentUser AuthUser user) {
        customerService.setDefaultAddress(user.getEmail(), id);
        return ResponseEntity.ok(Map.of("message", "Default address updated"));
    }
}
