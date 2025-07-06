package com.tuum.common.dto.mq;

import com.tuum.common.types.ErrorCode;
import java.time.LocalDateTime;

public class ErrorNotification {
    private ErrorCode errorCode;
    private String errorMessage;
    private String details;
    private LocalDateTime timestamp;
    private String requestId;

    public ErrorNotification() {}

    public ErrorNotification(ErrorCode errorCode, String errorMessage, String details, LocalDateTime timestamp, String requestId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public void setErrorCode(ErrorCode errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}