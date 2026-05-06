package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.entity.RefreshToken;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.exception.InvalidTokenException;
import com.nukkadseva.nukkadsevabackend.exception.TokenExpiredException;
import com.nukkadseva.nukkadsevabackend.exception.TokenRevokedException;
import com.nukkadseva.nukkadsevabackend.repository.RefreshTokenRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;

    private static final long REFRSH_TOKEN_EXPIRY = 7L;

    public RefreshTokenServiceImpl(RefreshTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        Users user = userRepository.getReferenceById(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(REFRSH_TOKEN_EXPIRY, ChronoUnit.DAYS));
        refreshToken.setUser(user);

        return tokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (savedToken.isRevoked()) {
            log.warn("Attempt to reuse revoked refresh token: {}", token);
            throw new TokenRevokedException("Refresh token revoked. Please login again.");
        }

        if (savedToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(savedToken);
            throw new TokenExpiredException("Refresh token expired. Please login again.");
        }

        return savedToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // detecting reuse of refresh token
        if (refreshToken.isRevoked()) {
            log.warn("Attempt to reuse refresh token: {}", token);
            throw new TokenRevokedException("Refresh token already used.");
        }

        // handle expiry(cleanup) in verify method, so that we can detect reuse of refresh token
        Instant now = Instant.now();
        if (refreshToken.getExpiryDate() != null && refreshToken.getExpiryDate().isBefore(now)) {
            tokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired. Please login again.");
        }

        refreshToken.setRevoked(true);
        tokenRepository.save(refreshToken);
    }
}
