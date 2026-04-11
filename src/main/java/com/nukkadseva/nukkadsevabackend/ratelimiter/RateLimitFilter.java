package com.nukkadseva.nukkadsevabackend.ratelimiter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitPolicyResolver policyResolver;
    private final RateLimitKeyResolver keyResolver;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        RateLimitType type = policyResolver.resolve(request);

        if (type == RateLimitType.NONE) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = keyResolver.resolve(request);

        Bucket bucket = rateLimitService.resolveBucket(key, type);

        if (bucket == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (bucket.tryConsume(1)) {
            response.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));

            filterChain.doFilter(request, response);
            return;
        } else {
            log.warn("Rate Limit exceeded | key={} | uri={}", key, uri);
            long retryAfter = 60;
            response.setStatus(429);
            response.setContentType("application/json");

            response.addHeader("X-RateLimit-Limit", rateLimitService.getLimitAsString(type));
            response.addHeader("X-RateLimit-Remaining", "0");
            response.addHeader("Retry-After", String.valueOf(retryAfter));

            String body = """
                    {
                        "timestamp": "%s",
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Rate Limit exceeded. Please try again later.",
                        "path": "%s"
                    }
                    """.formatted(Instant.now(), uri);

            response.getWriter().write(body);
        }
    }
}
