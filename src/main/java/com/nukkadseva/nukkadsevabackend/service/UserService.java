package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AuthResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    AuthResponse login(UserRequest userRequest);
    boolean verifyEmail(String token);
    void updateProfilePicture(MultipartFile file, Authentication authentication);
}
