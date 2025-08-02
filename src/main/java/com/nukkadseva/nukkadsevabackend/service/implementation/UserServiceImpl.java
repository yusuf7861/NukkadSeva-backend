package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.EmailAlreadyExistsException;
import com.nukkadseva.nukkadsevabackend.exception.UserAuthenticationException;
import com.nukkadseva.nukkadsevabackend.jwt.JwtOtpUtil;
import com.nukkadseva.nukkadsevabackend.jwt.JwtUtil;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.service.userservice.AppUserDetailsService;
import com.nukkadseva.nukkadsevabackend.service.userservice.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtOtpUtil jwtOtpUtil;

    @Override
    public void customerRegistration(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }

        Users users = new Users();
        users.setEmail(userRequest.getEmail());
        users.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        users.setRole(Role.CUSTOMER);

        userRepository.save(users);
    }

    @Override
    public String customerLogin(UserRequest customerRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            customerRequest.getEmail(), customerRequest.getPassword()
                    )
            );
            final UserDetails userDetails = userDetailsService.loadUserByUsername(customerRequest.getEmail());

            final String token = jwtUtil.generateToken(userDetails);
            return token;
        } catch (AuthenticationException e) {
            throw new UserAuthenticationException("Invalid email or password");
        }
    }

    @Override
    public boolean verifyOtp(VerifyOtpRequest request) {
        try {
            Claims claims = jwtOtpUtil.validateTokenAndGetClaims(token);
            String otpFromToken = (String) claims.get("otp");
            return otp.equals(otpFromToken);
        } catch (Exception e) {
            return false;
        }
    }
}
