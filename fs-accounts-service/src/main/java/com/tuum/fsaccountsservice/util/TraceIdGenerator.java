package com.tuum.fsaccountsservice.util;

import java.util.UUID;

public class TraceIdGenerator {
    
    private static final ThreadLocal<String> currentTraceId = new ThreadLocal<>();
    
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString();
        currentTraceId.set(traceId);
        return traceId;
    }
    
    public static String getCurrentTraceId() {
        String traceId = currentTraceId.get();
        if (traceId == null) {
            traceId = generateTraceId();
        }
        return traceId;
    }
    
    public static void setCurrentTraceId(String traceId) {
        currentTraceId.set(traceId);
    }
    
    public static void clearCurrentTraceId() {
        currentTraceId.remove();
    }
} 