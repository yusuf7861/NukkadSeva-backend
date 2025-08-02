package com.nukkadseva.nukkadsevabackend.dto.request;

import com.nukkadseva.nukkadsevabackend.entity.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Users}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest implements Serializable {
    @Email(message = "Email should be valid.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    @NotBlank(message = "Email is required. ")
    String email;
    @NotBlank(message = "Password is required. ")
    String password;
}