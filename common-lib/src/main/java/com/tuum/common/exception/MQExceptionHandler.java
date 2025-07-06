package com.tuum.common.exception;

import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.common.types.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
@Slf4j
public class MQExceptionHandler {


    @ExceptionHandler(ProcessingException.class)
    public ResponseEntity<ErrorNotification> handleProcessingException(ProcessingException ex) {
        log.error("Processing exception occurred: {}", ex.getMessage(), ex);
        ErrorNotification errorNotification = new ErrorNotification(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                ex.getRequestId(),
                LocalDateTime.now(),
                ex.getRequestId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNotification);
    }
    

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorNotification> handleBusinessException(BusinessException ex) {
        log.error("Business exception in MQ context: {}", ex.getMessage(), ex);
        ErrorNotification errorNotification = new ErrorNotification(
                ex.getErrorCode() != null ? ErrorCode.fromCode(ex.getErrorCode()) : ErrorCode.BUSINESS_ERROR,
                ex.getMessage(),
                "Business rule violation",
                LocalDateTime.now(),
                com.tuum.common.util.TraceIdGenerator.getCurrentTraceId()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(errorNotification);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorNotification> handleValidationException(ValidationException ex) {
        log.error("Validation exception in MQ context: {}", ex.getMessage(), ex);
        ErrorNotification errorNotification = new ErrorNotification(
                ex.getErrorCode() != null ? ErrorCode.fromCode(ex.getErrorCode()) : ErrorCode.VALIDATION_ERROR,
                ex.getMessage(),
                "Validation failed",
                LocalDateTime.now(),
                com.tuum.common.util.TraceIdGenerator.getCurrentTraceId()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(errorNotification);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorNotification> handleGenericException(Exception ex) {
        log.error("Unexpected error in MQ context: {}", ex.getMessage(), ex);
        ErrorNotification errorNotification = new ErrorNotification(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred during MQ processing",
                "Unexpected error in MQ processing",
                LocalDateTime.now(),
                com.tuum.common.util.TraceIdGenerator.getCurrentTraceId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNotification);
    }
} 