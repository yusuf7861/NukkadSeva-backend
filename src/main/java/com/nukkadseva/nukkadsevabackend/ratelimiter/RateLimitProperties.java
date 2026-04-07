package com.nukkadseva.nukkadsevabackend.ratelimiter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
@Getter
@Setter
public class RateLimitProperties {
    private Limit auth;
    private Limit publicApi;
    private Limit user;
    private Limit admin_api;
    private Limit booking;

    @Getter
    @Setter
    public static class Limit {
        private int capacity;
        private int refill;
        private int duration;
    }
}
