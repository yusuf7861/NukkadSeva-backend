package com.nukkadseva.nukkadsevabackend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token must not be blank")
        @JsonProperty("refresh_token")
        String refreshToken
) {
}
