package com.tuum.fsaccountsservice.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tuum.fsaccountsservice.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        log.error("Business exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .status(ex.getHttpStatus())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        log.error("Validation exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .status(ex.getHttpStatus())
                .path(getRequestPath(request))
                .validationErrors(ex.getValidationErrors())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .status(ex.getHttpStatus())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
        log.error("Insufficient funds exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .status(ex.getHttpStatus())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Method argument validation failed: {}", ex.getMessage(), ex);
        
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToValidationError)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message("Validation failed")
                .error("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(getRequestPath(request))
                .validationErrors(validationErrors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("HTTP message not readable exception occurred: {}", ex.getMessage(), ex);
        
        String message = "Invalid request format";
        Throwable cause = ex.getCause();
        
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            if (ife.getMessage().contains("Currency")) {
                message = "Invalid currency provided. Valid currencies are: EUR, SEK, GBP, USD";
            } else {
                message = ife.getMessage();
            }
        } else if (cause instanceof JsonMappingException) {
            message = cause.getMessage();
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(message)
                .error("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ErrorResponse> handleJsonMappingException(JsonMappingException ex, WebRequest request) {
        log.error("JSON mapping exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .error("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException ex, WebRequest request) {
        log.error("Invalid format exception occurred: {}", ex.getMessage(), ex);
        
        String message = ex.getMessage();
        if (message.contains("Currency")) {
            message = "Invalid currency provided. Valid currencies are: EUR, SEK, GBP, USD";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(message)
                .error("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .error("INVALID_ARGUMENT")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(generateTraceId())
                .timestamp(LocalDateTime.now())
                .message("An unexpected error occurred. Please try again later.")
                .error("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(getRequestPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String generateTraceId() {
        return TraceIdGenerator.getCurrentTraceId();
    }

    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private ErrorResponse.ValidationError mapToValidationError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString() : null)
                .build();
    }
} 