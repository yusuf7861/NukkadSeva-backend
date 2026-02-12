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

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("/**")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

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
    public ResponseEntity<?> updateCustomerProfile(@RequestBody CustomerProfileUpdateRequest request, @CurrentUser AuthUser user) {
        customerService.updateCustomerProfile(request, user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}
