package com.nukkadseva.nukkadsevabackend.service;

public interface EmailService {
    void sendForgotPasswordOtpEmail(String to, String name, String otp);

    void sendProviderApprovalEmail(String to, String password);

    void sendProviderRejectionEmail(String to, String name, String reason);

    void sendProviderVerificationEmail(String to, String token, Long providerId, String baseUrl);

    void sendCustomerVerificationEmail(String to, String token, String baseUrl);
}
