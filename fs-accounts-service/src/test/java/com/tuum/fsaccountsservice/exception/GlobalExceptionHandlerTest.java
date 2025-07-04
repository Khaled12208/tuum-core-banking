package com.tuum.fsaccountsservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        // Given
        BusinessException ex = new BusinessException("Test business error", "TEST_ERROR", 400);
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test business error", response.getBody().getMessage());
        assertEquals("TEST_ERROR", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getTraceId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", "123");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Account not found with id: 123", response.getBody().getMessage());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getError());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void testHandleInsufficientFundsException() {
        // Given
        InsufficientFundsException ex = new InsufficientFundsException("123", "USD");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientFundsException(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Insufficient funds in account 123 for currency USD", response.getBody().getMessage());
        assertEquals("INSUFFICIENT_FUNDS", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleValidationException() {
        // Given
        ValidationException ex = new ValidationException("Validation failed");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("VALIDATION_ERROR", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }
} 