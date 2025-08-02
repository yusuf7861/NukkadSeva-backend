package com.nukkadseva.nukkadsevabackend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.nukkadseva.nukkadsevabackend.entity.Users}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {
    String email;
    String password;
    String role;
}