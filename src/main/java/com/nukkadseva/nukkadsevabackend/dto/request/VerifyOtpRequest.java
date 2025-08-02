package com.nukkadseva.nukkadsevabackend.dto.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String otp;
    private String token;
}
