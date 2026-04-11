package com.nukkadseva.nukkadsevabackend.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyResolver {

    public String resolve(HttpServletRequest request) {
        String userId = getUserId();

        if (userId != null) {
            return "USER_" + userId;
        }

        return "IP" + request.getRemoteAddr();
    }

    public String getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return null;
    }
}
