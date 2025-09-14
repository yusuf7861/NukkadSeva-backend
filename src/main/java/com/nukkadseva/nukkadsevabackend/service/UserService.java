package com.nukkadseva.nukkadsevabackend.service;

import java.io.IOException;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;

import com.nukkadseva.nukkadsevabackend.entity.Customers;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;

public interface UserService {
    String login(UserRequest userRequest);
    String sendVerificationOtp(String email) throws MessagingException, IOException, TemplateException;
    boolean verifyOtp(VerifyOtpRequest request);

}
