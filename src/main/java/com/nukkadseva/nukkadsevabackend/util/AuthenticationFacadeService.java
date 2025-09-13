package com.nukkadseva.nukkadsevabackend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacadeService implements com.nukkadseva.nukkadsevabackend.service.AuthenticationFacade {
    @Override
    public Authentication getAuthenticationFacade() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
