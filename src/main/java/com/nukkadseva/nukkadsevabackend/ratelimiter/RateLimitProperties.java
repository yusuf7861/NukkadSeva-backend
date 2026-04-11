package com.nukkadseva.nukkadsevabackend.ratelimiter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "rate-limit")
@Validated
@Getter
@Setter
public class RateLimitProperties {
    @Valid
    @NotNull
    private Limit auth;

    @Valid
    @NotNull
    private Limit publicApi;

    @Valid
    @NotNull
    private Limit user;

    @Valid
    @NotNull
    private Limit adminApi;

    @Valid
    @NotNull
    private Limit booking;

    @Getter
    @Setter
    public static class Limit {
        @Min(1)
        private int capacity;

        @Min(1)
        private int refill;

        @Min(1)
        private int duration;
    }
}
