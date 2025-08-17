package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.ApiResponse;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.dto.response.OtpTokenResponse;
import com.nukkadseva.nukkadsevabackend.exception.InvalidOtpException;
import com.nukkadseva.nukkadsevabackend.service.userservice.UserService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("/**")
public class UserController {
    private final UserService userService;

    @PostMapping("/customer/register")
    public ResponseEntity<Map<String, Object>> customerRegistration(@Valid @RequestBody UserRequest userRequest) {
        userService.customerRegistration(userRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("httpStatusCode", HttpStatus.CREATED.value());
        response.put("message", "User registered successfully.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserRequest userRequest, HttpServletResponse response) {
        String token = userService.login(userRequest);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("none")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new AuthResponse(token, userRequest.getEmail()));
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


    @GetMapping
    public String welcom() {
        return "Welcome to Nukkadseva";
    }
}
