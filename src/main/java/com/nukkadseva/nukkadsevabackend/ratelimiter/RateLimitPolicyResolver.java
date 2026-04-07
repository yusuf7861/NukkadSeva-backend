package com.nukkadseva.nukkadsevabackend.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RateLimitPolicyResolver {
    public RateLimitType resolve(HttpServletRequest httpServletRequest) {
        String uri = httpServletRequest.getRequestURI();

        // skip rate limiting for actuator endpoints
        if (uri.startsWith("/actuator")) {
            return RateLimitType.NONE;
        }

        // AUTHENTICATED USER ENDPOINTS
        if (uri.contains("/api/login") ||
            uri.contains("/api/reset-password") || uri.contains("/api/forggot-password") || uri.contains("/api/verify-email") || uri.contains("/api/auth/google")) {
            return RateLimitType.AUTH_API;
        }

        // PUBLIC ENDPOINTS
        if (uri.startsWith("/api/public")) {
            return RateLimitType.PUBLIC_API;
        }

        // ADMIN ENDPOINTS
        if (uri.startsWith("/api/admin")) {
            return RateLimitType.ADMIN_API;
        }

        // BOOKING ENDPOINTS
        if (uri.startsWith("/api/booking")) {
            return RateLimitType.BOOKING_API;
        }

        // DEFAULT
        if (uri.startsWith("/api")) {
            return RateLimitType.USER_API;
        }

        // For all other endpoints, apply no rate limiting
        return RateLimitType.NONE;


    }
}
