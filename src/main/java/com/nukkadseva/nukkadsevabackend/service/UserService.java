package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.UserResponse;
import com.nukkadseva.nukkadsevabackend.entity.Users;

public interface UserService {
    Users customerRegisteration(UserRequest userRequest);
}
