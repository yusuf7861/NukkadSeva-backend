package com.nukkadseva.nukkadsevabackend.exception;

import com.nukkadseva.nukkadsevabackend.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return buildErrorResponse("EMAIL_ALREADY_EXISTS", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ApiError> handleUserAuthenticationException(UserAuthenticationException e) {
        return buildErrorResponse("AUTHENTICATION_FAILED", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiError> handleInvalidOtpException(InvalidOtpException e) {
        return buildErrorResponse("INVALID_OTP", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception e) {
        return buildErrorResponse("INTERNAL_SERVER_ERROR", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildErrorResponse(String code, String message, HttpStatus status) {
        return new ResponseEntity<>(new ApiError(code, message, status), status);
    }
}
