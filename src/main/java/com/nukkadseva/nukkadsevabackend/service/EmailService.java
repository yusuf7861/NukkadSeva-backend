package com.nukkadseva.nukkadsevabackend.service;

public interface EmailService {
    void sendForgotPasswordOtpEmail(String to, String name, String otp);
}
