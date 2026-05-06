package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyRefreshToken(String token);
    void revokeRefreshToken(String token);
}
