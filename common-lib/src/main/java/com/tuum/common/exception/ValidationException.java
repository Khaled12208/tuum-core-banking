package com.tuum.common.exception;

import com.tuum.common.dto.ErrorResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends BusinessException {
    
    private final List<ErrorResponse.ValidationError> validationErrors;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = List.of();
    }
    
    public ValidationException(String message, List<ErrorResponse.ValidationError> validationErrors) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(List<ErrorResponse.ValidationError> validationErrors) {
        super("Validation failed", "VALIDATION_ERROR", 400);
        this.validationErrors = validationErrors;
    }
} 