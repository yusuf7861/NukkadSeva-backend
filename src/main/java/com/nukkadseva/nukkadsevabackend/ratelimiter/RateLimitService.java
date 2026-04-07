package com.nukkadseva.nukkadsevabackend.ratelimiter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> cache;
    private final RateLimitProperties properties;

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(15))
                .maximumSize(50_000)
                .build();
    }

    public Bucket resolveBucket(String key, RateLimitType type) {
        return cache.get(key + "_" + type, k -> createBucket(type));
    }

    public Bucket createBucket(RateLimitType type) {
        RateLimitProperties.Limit config = getConfig(type);

        if (config == null) return null;

        Bandwidth limit = Bandwidth.classic(
                config.getCapacity(),
                Refill.intervally(
                        config.getRefill(),
                        Duration.ofSeconds(config.getDuration())
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private RateLimitProperties.Limit getConfig(RateLimitType type) {
        return switch (type) {
            case AUTH_API -> properties.getAuth();
            case PUBLIC_API -> properties.getPublicApi();
            case USER_API -> properties.getUser();
            case BOOKING_API -> properties.getBooking();
            case ADMIN_API -> properties.getAdmin_api();
            default -> null;
        };
    }

    public String getLimitAsString(RateLimitType type) {
        RateLimitProperties.Limit config = getConfig(type);

        if (config == null) return "0";

        return String.valueOf(config.getCapacity());
    }
}
