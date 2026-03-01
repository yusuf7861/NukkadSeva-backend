package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.UserAuthenticationException;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.security.JwtUtil;
import com.nukkadseva.nukkadsevabackend.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.nukkadseva.nukkadsevabackend.service.EmailService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRequest userRequest;
    private Users mockUser;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setEmail("test@test.com");
        userRequest.setPassword("Password123!");

        mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setVerified(true);
        mockUser.setRole(Role.CUSTOMER);

        Customers customer = new Customers();
        customer.setId(1L);
        mockUser.setCustomers(customer);
    }

    @Test
    void testLogin_Success() {
        Authentication mockAuthentication = mock(Authentication.class);
        org.springframework.security.core.userdetails.UserDetails mockUserDetails = mock(
                org.springframework.security.core.userdetails.UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
        when(mockUserDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(1L, "test@test.com", "CUSTOMER", 1L)).thenReturn("mockJwtToken");

        AuthResponse response = authService.login(userRequest);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void testLogin_UserNotVerified_ThrowsException() {
        mockUser.setVerified(false);
        Authentication mockAuthentication = mock(Authentication.class);
        org.springframework.security.core.userdetails.UserDetails mockUserDetails = mock(
                org.springframework.security.core.userdetails.UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
        when(mockUserDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        UserAuthenticationException exception = assertThrows(UserAuthenticationException.class, () -> {
            authService.login(userRequest);
        });

        assertEquals("Please verify your email before logging in", exception.getMessage());
    }

    @Test
    void testLogin_BadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UserAuthenticationException exception = assertThrows(UserAuthenticationException.class, () -> {
            authService.login(userRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }
}
