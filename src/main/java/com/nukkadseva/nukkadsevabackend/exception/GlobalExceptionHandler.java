package com.nukkadseva.nukkadsevabackend.exception;

import com.nukkadseva.nukkadsevabackend.dto.ApiError;
import org.springframework.dao.DataAccessException;
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
        return buildErrorResponse("BOOKING_FAILED",  e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(DataAccessException e) {
        return buildErrorResponse("DATABASE_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
