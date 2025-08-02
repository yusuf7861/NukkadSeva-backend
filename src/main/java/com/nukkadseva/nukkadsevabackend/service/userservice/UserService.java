package com.nukkadseva.nukkadsevabackend.service.userservice;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import com.nukkadseva.nukkadsevabackend.entity.Users;

public interface UserService {
    void customerRegistration(UserRequest userRequest);
    String customerLogin(UserRequest userRequest);
    boolean verifyOtp(VerifyOtpRequest request);
}
