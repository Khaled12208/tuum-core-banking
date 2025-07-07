package com.tuum.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.tuum.common.dto.ErrorResponse;
import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.common.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("Business exception [{}]: {} - Error code: {}", traceId, ex.getMessage(), ex.getErrorCode());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .status(ex.getHttpStatus())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("Resource not found [{}]: {}", traceId, ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("Insufficient funds [{}]: {}", traceId, ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error("INSUFFICIENT_FUNDS")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("Validation error [{}]: {}", traceId, ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            validationErrors.add(new ErrorResponse.ValidationError(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getRejectedValue() != null ? error.getRejectedValue().toString() : null
            ));
        });

        // Create a user-friendly message for missing mandatory parameters
        String userMessage = "Request validation failed";
        if (!validationErrors.isEmpty()) {
            List<String> missingFields = validationErrors.stream()
                    .filter(error -> error.getMessage() != null && 
                            (error.getMessage().contains("mandatory") || 
                             error.getMessage().contains("required") ||
                             error.getMessage().contains("must not be")))
                    .map(ErrorResponse.ValidationError::getField)
                    .collect(Collectors.toList());
            
            if (!missingFields.isEmpty()) {
                userMessage = "Missing mandatory parameters: " + String.join(", ", missingFields);
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error("VALIDATION_ERROR")
                .message(userMessage)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("Constraint violation [{}]: {}", traceId, ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            validationErrors.add(new ErrorResponse.ValidationError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage(),
                    violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null
            ));
        });

        // Create a user-friendly message for missing mandatory parameters
        String userMessage = "Request validation failed";
        String errorCode = "VALIDATION_ERROR";
        
        if (!validationErrors.isEmpty()) {
            // Check for header validation errors
            boolean hasHeaderError = validationErrors.stream()
                    .anyMatch(error -> error.getField() != null && 
                            (error.getField().contains("idempotencyKey") || 
                             error.getField().contains("Idempotency-Key")));
            
            if (hasHeaderError) {
                userMessage = "Missing or invalid Idempotency-Key header";
                errorCode = "MISSING_IDEMPOTENCY_KEY";
            } else {
                // Check for body parameter validation errors
                List<String> missingFields = validationErrors.stream()
                        .filter(error -> error.getMessage() != null && 
                                (error.getMessage().contains("mandatory") || 
                                 error.getMessage().contains("required") ||
                                 error.getMessage().contains("must not be")))
                        .map(ErrorResponse.ValidationError::getField)
                        .collect(Collectors.toList());
                
                if (!missingFields.isEmpty()) {
                    userMessage = "Missing mandatory parameters: " + String.join(", ", missingFields);
                }
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error(errorCode)
                .message(userMessage)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("HTTP message not readable [{}]: {}", traceId, ex.getMessage());
        
        String userMessage = "Invalid request format";
        
        // Check if this is a currency-related JsonMappingException
        if (ex.getCause() instanceof JsonMappingException jsonEx) {
            String message = jsonEx.getMessage();
            if (message != null && (message.contains("Invalid currency") || message.contains("currency"))) {
                log.error("Invalid currency error [{}]: {}", traceId, message);
                userMessage = "Invalid currency provided. Valid currencies are: EUR, SEK, GBP, USD";
            } else if (message != null && message.contains("Cannot deserialize")) {
                userMessage = "Invalid data format in request";
            } else if (message != null && message.contains("Missing required creator property")) {
                userMessage = "Missing mandatory parameters in request body";
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error("INVALID_REQUEST_FORMAT")
                .message(userMessage)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        log.error("HTTP media type not supported [{}]: {}", traceId, ex.getMessage());
        
        String userMessage = "Content type not supported. Expected 'application/json'";
        
        // Check if this might be related to invalid currency in the request
        String message = ex.getMessage();
        if (message != null && message.contains("currency")) {
            log.error("Possible invalid currency in media type error [{}]: {}", traceId, message);
            userMessage = "Invalid currency provided. Valid currencies are: EUR, SEK, GBP, USD";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error("UNSUPPORTED_MEDIA_TYPE")
                .message(userMessage)
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String traceId = TraceIdGenerator.generateTraceId();
        
        // Log the full exception for debugging purposes
        log.error("Unexpected error occurred [{}]: {}", traceId, ex.getMessage(), ex);
        
        // Return a clean, user-friendly error message without exposing internal details
        ErrorResponse errorResponse = ErrorResponse.builder()
                .traceId(traceId)
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 