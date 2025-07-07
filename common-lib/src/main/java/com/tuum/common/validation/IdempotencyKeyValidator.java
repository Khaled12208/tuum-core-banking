package com.tuum.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdempotencyKeyValidator implements ConstraintValidator<ValidIdempotencyKey, String> {
    
    @Override
    public void initialize(ValidIdempotencyKey constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String idempotencyKey, ConstraintValidatorContext context) {
        return idempotencyKey != null && !idempotencyKey.trim().isEmpty();
    }
} 