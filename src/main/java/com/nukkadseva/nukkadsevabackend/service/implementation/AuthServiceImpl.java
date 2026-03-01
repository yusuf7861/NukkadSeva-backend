package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.ForgotPasswordRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.ResetPasswordRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.exception.CustomerNotFoundException;
import com.nukkadseva.nukkadsevabackend.exception.InvalidOtpException;
import com.nukkadseva.nukkadsevabackend.exception.UserAuthenticationException;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.security.JwtUtil;
import com.nukkadseva.nukkadsevabackend.service.AuthService;
import com.nukkadseva.nukkadsevabackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(UserRequest userRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userRequest.getEmail(),
                            userRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Fetch user to get ID and profile info
            Users user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UserAuthenticationException("User not found"));

            // Block login if user is not verified
            if (!user.isVerified()) {
                throw new UserAuthenticationException("Please verify your email before logging in");
            }

            // Get role without ROLE_ prefix for JWT
            String role = user.getRole().name();

            // Get profile ID based on role (customer or provider)
            Long profileId = getProfileId(user);

            // Generate token with user claims
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), role, profileId);

            return AuthResponse.builder()
                    .accessToken(token)
                    .refreshToken(null) // TODO: Implement refresh token
                    .tokenType("Bearer")
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", userRequest.getEmail());
            throw new UserAuthenticationException("Invalid email or password");
        } catch (DisabledException e) {
            throw new UserAuthenticationException("Account is disabled");
        } catch (LockedException e) {
            throw new UserAuthenticationException("Account is locked");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for email: {}", userRequest.getEmail(), e);
            throw new UserAuthenticationException("Authentication failed");
        }
    }

    private Long getProfileId(Users user) {
        if (user.getCustomers() != null) {
            return user.getCustomers().getId();
        } else if (user.getProvider() != null) {
            return user.getProvider().getId();
        }
        return null;
    }

    @Override
    @Transactional
    public void generateResetOtp(ForgotPasswordRequest request) {
        Optional<Users> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new CustomerNotFoundException("No account found with this email address.");
        }

        Users user = userOpt.get();

        if (!user.isVerified()) {
            throw new CustomerNotFoundException(
                    "This account has not been verified yet. Please verify your email first.");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));

        user.setVerificationToken(otp);
        user.setTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Get user's name for email if available
        String name = "User";
        if (user.getCustomers() != null && user.getCustomers().getFullName() != null) {
            name = user.getCustomers().getFullName();
        } else if (user.getProvider() != null && user.getProvider().getFullName() != null) {
            name = user.getProvider().getFullName();
        }

        emailService.sendForgotPasswordOtpEmail(user.getEmail(), name, otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("No account found with this email address."));

        if (user.getVerificationToken() == null || !user.getVerificationToken().equals(request.getOtp())) {
            throw new InvalidOtpException("The OTP you entered is incorrect. Please try again.");
        }

        if (user.getTokenExpiresAt() == null || user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("This OTP has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationToken(null);
        user.setTokenExpiresAt(null);
        userRepository.save(user);
    }
}
