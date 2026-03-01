package com.nukkadseva.nukkadsevabackend.service.implementation;

import java.time.LocalDateTime;
import java.util.Optional;

import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.dto.request.ForgotPasswordRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.ResetPasswordRequest;
import com.nukkadseva.nukkadsevabackend.service.EmailService;
import com.nukkadseva.nukkadsevabackend.service.AzureBlobStorageService;
import com.nukkadseva.nukkadsevabackend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.exception.UserAuthenticationException;
import com.nukkadseva.nukkadsevabackend.exception.CustomerNotFoundException;
import com.nukkadseva.nukkadsevabackend.exception.InvalidOtpException;
import com.nukkadseva.nukkadsevabackend.security.JwtUtil;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;

import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticationManager authenticationManager;
    private final ProviderRepository providerRepository;
    private final JwtUtil jwtUtil;
    private final AzureBlobStorageService azureBlobStorageService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean verifyEmail(String token) {
        Optional<Users> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }
        Users user = userOpt.get();
        if (user.isVerified()) {
            return true;
        }
        if (user.getTokenExpiresAt() != null && user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiresAt(null);
        userRepository.save(user);
        return true;
    }

    @Transactional
    @Override
    public void updateProfilePicture(MultipartFile file, Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .orElseThrow(() -> new RuntimeException("No role assigned"));

        switch (role) {
            case "CUSTOMER":
                Customers customer = customerRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Customer not found"));

                String imageLink = azureBlobStorageService.uploadFile(file, "profilePicture");
                customer.setPhotograph(imageLink);
                customerRepository.save(customer);
                break;

            case "SERVICE_PROVIDER":
                Provider provider = providerRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Provider not found"));

                String providerImageLink = azureBlobStorageService.uploadFile(file, "profilePictures");
                provider.setPhotograph(providerImageLink);
                providerRepository.save(provider);
                break;

            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }

}
