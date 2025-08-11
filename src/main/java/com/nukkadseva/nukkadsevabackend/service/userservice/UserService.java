package com.nukkadseva.nukkadsevabackend.service.userservice;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface UserService {
    void customerRegistration(UserRequest userRequest);
    String login(UserRequest userRequest);
    String sendVerificationOtp(String email) throws MessagingException, IOException, TemplateException;
    boolean verifyOtp(VerifyOtpRequest request);

}
