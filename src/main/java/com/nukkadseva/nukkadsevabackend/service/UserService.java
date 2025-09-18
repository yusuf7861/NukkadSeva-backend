package com.nukkadseva.nukkadsevabackend.service;

import java.io.IOException;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;

import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    AuthResponse login(UserRequest userRequest);
    String sendVerificationOtp(String email) throws MessagingException, IOException, TemplateException;
    boolean verifyOtp(VerifyOtpRequest request);
    void updateProfilePicture(MultipartFile file, Authentication authentication);
}
