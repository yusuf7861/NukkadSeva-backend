package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.ForgotPasswordRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.ResetPasswordRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(UserRequest userRequest);

    void generateResetOtp(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
