package com.nukkadseva.nukkadsevabackend.exception;

import com.nukkadseva.nukkadsevabackend.dto.ApiError;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Re-throw Spring Security exceptions so they are handled by
    // CustomAccessDeniedHandler / CustomAuthenticationEntryPoint
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e) throws AccessDeniedException {
        throw e;
    }

    @ExceptionHandler(AuthenticationException.class)
    public void handleAuthenticationException(AuthenticationException e) throws AuthenticationException {
        throw e;
    }

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

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiError> handleFileNotFoundException(FileNotFoundException e) {
        return buildErrorResponse("NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ApiError> handleFileSizeException(FileSizeExceededException e) {
        return buildErrorResponse("FILE_SIZE_EXCEEDED", e.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ApiError> handleInvalidFileTypeException(InvalidFileTypeException e) {
        return buildErrorResponse("INVALID_FILE_TYPE", e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(ProviderNotFoundException.class)
    public ResponseEntity<ApiError> handleProviderNotFoundException(ProviderNotFoundException e) {
        return buildErrorResponse("PROVIDER_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiError> handleCustomerNotFoundException(CustomerNotFoundException e) {
        return buildErrorResponse("CUSTOMER_NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingCreationException.class)
    public ResponseEntity<ApiError> handleBookingCreationException(BookingCreationException e) {
        return buildErrorResponse("BOOKING_FAILED", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(DataAccessException e) {
        return buildErrorResponse("DATABASE_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildErrorResponse("VALIDATION_ERROR", errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        return buildErrorResponse("MALFORMED_JSON_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidTokenException(InvalidTokenException e) {
        return buildErrorResponse("INVALID_TOKEN", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<ApiError> handleTokenRevokedException(TokenRevokedException e) {
        return buildErrorResponse("TOKEN_REVOKED", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleTokenExpiredException(TokenExpiredException e) {
        return buildErrorResponse("TOKEN_EXPIRED", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // define all methods above 👆👆👆👆
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception e) {
        return buildErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildErrorResponse(String code, String message, HttpStatus status) {
        return new ResponseEntity<>(new ApiError(code, message, status), status);
    }
}
