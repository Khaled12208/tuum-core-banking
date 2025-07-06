package com.tuum.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProcessingException extends RuntimeException {
    private final String exchangeName;
    private final String routingKey;
    private final String messageType;
    private final String requestId;
    private final Map<String, Object> extraHeaders;

    public ProcessingException(
            String message,
            String exchangeName,
            String routingKey,
            String messageType,
            String requestId,
            Map<String, Object> extraHeaders
    ) {
        super(message);
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.messageType = messageType;
        this.requestId = requestId;
        this.extraHeaders = extraHeaders;
    }
} 