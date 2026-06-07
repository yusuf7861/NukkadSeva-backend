package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.response.RefreshTokenResponse;
import com.nukkadseva.nukkadsevabackend.entity.RefreshToken;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.exception.InvalidTokenException;
import com.nukkadseva.nukkadsevabackend.exception.TokenExpiredException;
import com.nukkadseva.nukkadsevabackend.exception.TokenRevokedException;
import com.nukkadseva.nukkadsevabackend.repository.RefreshTokenRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.security.JwtUtil;
import com.nukkadseva.nukkadsevabackend.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final long REFRESH_TOKEN_EXPIRY = 7L;

    public RefreshTokenServiceImpl(RefreshTokenRepository tokenRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        Users user = userRepository.getReferenceById(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(REFRESH_TOKEN_EXPIRY, ChronoUnit.DAYS));
        refreshToken.setUser(user);

        return tokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken savedToken = tokenRepository.findByTokenWithUser(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        assertTokenValid(savedToken);

        return savedToken;
    }

    @Override
    public void revokeRefreshToken(String tokenString) {
        RefreshToken refreshToken = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // detecting reuse of refresh token
        if (refreshToken.isRevoked()) {
            log.warn("Attempt to reuse already-revoked refresh token (id: {})", refreshToken.getId());
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

    @Override
    @Transactional
    public RefreshTokenResponse rotateRefreshToken(String token) {
        RefreshToken savedToken = tokenRepository.findByTokenWithUser(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        assertTokenValid(savedToken);

        Users user = savedToken.getUser();

        // Revoke old token
        savedToken.setRevoked(true);
        tokenRepository.save(savedToken);

        // Create new refresh token
        RefreshToken newRt = createRefreshToken(user.getId());

        String newAccessToken = jwtUtil.generateToken(
                user.getId(), user.getEmail(), user.getRole().name(), resolveProfileId(user));

        return new RefreshTokenResponse(newAccessToken, newRt.getToken(), "Bearer");
    }

    /**
     * Validates that the token is not revoked and not expired.
     * Deletes the token from the repository if it has expired.
     */
    private void assertTokenValid(RefreshToken savedToken) {
        if (savedToken.isRevoked()) {
            log.warn("Attempt to reuse revoked refresh token for user id: {}", savedToken.getUser().getId());
            throw new TokenRevokedException("Refresh token revoked. Please login again.");
        }

        if (savedToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(savedToken);
            throw new TokenExpiredException("Refresh token expired. Please login again.");
        }
    }

    /**
     * Returns the profile id for the given user (Customer or Provider), or null if neither is set.
     */
    private Long resolveProfileId(Users user) {
        if (user.getCustomers() != null) {
            return user.getCustomers().getId();
        } else if (user.getProvider() != null) {
            return user.getProvider().getId();
        }
        return null;
    }
}
