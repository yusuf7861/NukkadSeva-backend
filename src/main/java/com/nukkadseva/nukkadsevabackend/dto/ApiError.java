package com.nukkadseva.nukkadsevabackend.dto;

public record ApiError(
        String statusCode,
        String message
) {
}
