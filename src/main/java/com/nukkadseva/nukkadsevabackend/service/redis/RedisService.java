package com.nukkadseva.nukkadsevabackend.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Profile(value = {"dev"})
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue()
                .set(key, value);
    }

    public void set(String key, Object value, long timeoutInSeconds) {
        redisTemplate.opsForValue()
                .set(key, value, Duration.ofSeconds(timeoutInSeconds));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
