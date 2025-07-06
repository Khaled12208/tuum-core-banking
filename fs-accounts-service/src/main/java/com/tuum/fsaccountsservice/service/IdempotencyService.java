package com.tuum.fsaccountsservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class IdempotencyService {
    
    private final ConcurrentMap<String, Boolean> processedKeys = new ConcurrentHashMap<>();
    
    public boolean isProcessed(String idempotencyKey) {
        return processedKeys.containsKey(idempotencyKey);
    }
    
    public void markAsProcessed(String idempotencyKey) {
        processedKeys.put(idempotencyKey, true);
        log.info("Marked idempotency key as processed: {}", idempotencyKey);
    }
    
    public void clearProcessed(String idempotencyKey) {
        processedKeys.remove(idempotencyKey);
        log.info("Cleared idempotency key from cache: {}", idempotencyKey);
    }
} 