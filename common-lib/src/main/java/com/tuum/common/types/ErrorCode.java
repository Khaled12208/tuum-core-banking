package com.tuum.common.types;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    // Account-related errors (1000-1999)
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", 404, "Account not found"),
    ACCOUNT_ALREADY_EXISTS("ACCOUNT_ALREADY_EXISTS", 409, "Account already exists"),
    ACCOUNT_CREATION_FAILED("ACCOUNT_CREATION_FAILED", 500, "Failed to create account"),
    INVALID_CUSTOMER_ID("INVALID_CUSTOMER_ID", 400, "Invalid customer ID"),
    INVALID_COUNTRY_CODE("INVALID_COUNTRY_CODE", 400, "Invalid country code"),
    INVALID_CURRENCY("INVALID_CURRENCY", 400, "Invalid currency"),
    
    // Transaction-related errors (2000-2999)
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND", 404, "Transaction not found"),
    TRANSACTION_ALREADY_EXISTS("TRANSACTION_ALREADY_EXISTS", 409, "Transaction already exists"),
    TRANSACTION_CREATION_FAILED("TRANSACTION_CREATION_FAILED", 500, "Failed to create transaction"),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", 400, "Insufficient funds"),
    INVALID_TRANSACTION_AMOUNT("INVALID_TRANSACTION_AMOUNT", 400, "Invalid transaction amount"),
    INVALID_TRANSACTION_DIRECTION("INVALID_TRANSACTION_DIRECTION", 400, "Invalid transaction direction"),
    BALANCE_NOT_FOUND("BALANCE_NOT_FOUND", 404, "Balance not found for the specified currency"),
    CONCURRENT_MODIFICATION("CONCURRENT_MODIFICATION", 409, "Balance was modified by another transaction. Please retry"),
    
    // Validation errors (3000-3999)
    VALIDATION_ERROR("VALIDATION_ERROR", 400, "Validation failed"),
    BUSINESS_ERROR("BUSINESS_ERROR", 400, "Business rule violation"),
    MISSING_IDEMPOTENCY_KEY("MISSING_IDEMPOTENCY_KEY", 400, "Idempotency-Key header is required"),
    INVALID_IDEMPOTENCY_KEY("INVALID_IDEMPOTENCY_KEY", 400, "Invalid idempotency key"),
    DUPLICATE_REQUEST("DUPLICATE_REQUEST", 409, "Request with this idempotency key has already been processed"),
    
    // System errors (5000-5999)
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500, "An unexpected error occurred"),
    DATABASE_ERROR("DATABASE_ERROR", 500, "Database operation failed"),
    MESSAGE_QUEUE_ERROR("MESSAGE_QUEUE_ERROR", 500, "Message queue operation failed"),
    PROCESSING_ERROR("PROCESSING_ERROR", 500, "Processing failed"),
    TIMEOUT_ERROR("TIMEOUT_ERROR", 408, "Request timed out"),
    
    // External service errors (6000-6999)
    EXTERNAL_SERVICE_UNAVAILABLE("EXTERNAL_SERVICE_UNAVAILABLE", 503, "External service is unavailable"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", 502, "External service error");
    
    private final String code;
    private final int httpStatus;
    private final String defaultMessage;
    
    ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
    
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR; // Default fallback
    }
} 