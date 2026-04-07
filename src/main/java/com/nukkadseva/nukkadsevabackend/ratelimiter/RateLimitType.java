package com.nukkadseva.nukkadsevabackend.ratelimiter;

public enum RateLimitType {
    AUTH_API,        // login, reset, verify
    PUBLIC_API,      // public endpoints
    USER_API,        // customer/provider APIs
    BOOKING_API,     // booking-related APIs
    ADMIN_API,       // admin operations
    NONE             // skip
}
