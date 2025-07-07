package com.tuum.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IdempotencyKeyValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIdempotencyKey {
    String message() default "Idempotency-Key header is required and cannot be empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 