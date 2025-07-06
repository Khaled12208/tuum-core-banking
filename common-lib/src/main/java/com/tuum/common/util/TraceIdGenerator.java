package com.tuum.common.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TraceIdGenerator {
    
    private static final ThreadLocal<String> currentRequestId = new ThreadLocal<>();
    
    public static String generateTraceId() {
        String requestId = UUID.randomUUID().toString();
        currentRequestId.set(requestId);
        return requestId;
    }
    
    public static String getCurrentTraceId() {
        String requestId = currentRequestId.get();
        if (requestId == null) {
            requestId = generateTraceId();
        }
        return requestId;
    }
    
    public static void setCurrentRequestId(String requestId) {
        currentRequestId.set(requestId);
    }
    
    public static String getCurrentRequestId() {
        return currentRequestId.get();
    }
    
    public static void clear() {
        currentRequestId.remove();
    }
} 