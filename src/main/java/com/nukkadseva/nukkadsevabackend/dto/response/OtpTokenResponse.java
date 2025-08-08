package com.nukkadseva.nukkadsevabackend.dto.response;

public record OtpTokenResponse(
        String status,
        String message,
        String token
) {}
