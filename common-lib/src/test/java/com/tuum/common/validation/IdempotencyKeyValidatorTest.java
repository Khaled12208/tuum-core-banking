package com.tuum.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdempotencyKeyValidatorTest {

    private IdempotencyKeyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IdempotencyKeyValidator();
    }

    @Test
    void testValidIdempotencyKey() {
        assertTrue(validator.isValid("valid-key-123", null));
        assertTrue(validator.isValid("req-123456", null));
        assertTrue(validator.isValid("uuid-123e4567-e89b-12d3-a456-426614174000", null));
    }

    @Test
    void testNullIdempotencyKey() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void testEmptyIdempotencyKey() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    void testBlankIdempotencyKey() {
        assertFalse(validator.isValid("   ", null));
        assertFalse(validator.isValid("\t\n", null));
    }
} 