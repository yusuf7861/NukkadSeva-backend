package com.nukkadseva.nukkadsevabackend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {
    private final Long userId;
    private final String email;
    private final String role;
    private final Long profileId;

    // authentication.getName() calls getPrincipal().toString()
    // so this must return the email for provider/customer lookup to work
    @Override
    public String toString() {
        return email;
    }
}
