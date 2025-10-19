package com.nukkadseva.nukkadsevabackend.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.nukkadseva.nukkadsevabackend.util.FileValidationUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.nukkadseva.nukkadsevabackend.dto.ApiResponse;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.dto.response.OtpTokenResponse;
import com.nukkadseva.nukkadsevabackend.exception.InvalidOtpException;
import com.nukkadseva.nukkadsevabackend.service.UserService;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserRequest userRequest, HttpServletResponse response) {
        AuthResponse login = userService.login(userRequest);

        ResponseCookie cookie = ResponseCookie.from("jwt", login.getToken())
                .httpOnly(true)
                .secure(false) //TODO: make true at the time of deployment
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("none")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(login);
    }

    @PostMapping("/send-verification-otp")
    public ResponseEntity<OtpTokenResponse> sendVerificationOtp(@RequestParam String email) throws MessagingException, TemplateException, IOException {
        String token = userService.sendVerificationOtp(email);
        return ResponseEntity.ok(new OtpTokenResponse("OTP_SENT", "OTP sent successfully.", token));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        boolean verified = userService.verifyOtp(request);
        if (verified) {
            return ResponseEntity.ok(new ApiResponse("OTP_VERIFIED", "OTP verified successfully."));
        } else {
            throw new InvalidOtpException("OTP verification failed.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        SecurityContextHolder.clearContext();

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("none")
                .build();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PutMapping("/update-profile-picture")
    public ResponseEntity<ApiResponse> updateProfileImage(@RequestParam("file") MultipartFile file, Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("401_UNAUTHORIZED", "User not authenticated."));
        }

        FileValidationUtil.validateProfilePicture(file);

        userService.updateProfilePicture(file, authentication);
        return ResponseEntity.ok()
                .body(new ApiResponse("PROFILE_UPDATED", "Profile Picture Updated Successfully"));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<AuthResponse> authMe(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("USER");

        AuthResponse authResponse = new AuthResponse(
                null,
                userDetails.getUsername(),
                role
        );

        return ResponseEntity.ok(authResponse);
    }
}
