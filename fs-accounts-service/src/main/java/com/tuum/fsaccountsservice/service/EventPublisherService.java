package com.tuum.fsaccountsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tuum.common.types.RabbitMQConfig;
import com.tuum.common.types.RequestType;
import com.tuum.common.types.ErrorCode;
import com.tuum.common.exception.BusinessException;
import com.tuum.fsaccountsservice.consumers.AccountNotificationConsumer;
import com.tuum.fsaccountsservice.consumers.TransactionNotificationConsumer;
import com.tuum.common.dto.mq.ErrorNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;
    private final AccountNotificationConsumer accountNotificationConsumer;
    private final TransactionNotificationConsumer transactionNotificationConsumer;

    private final ConcurrentHashMap<String, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();

    public <T> T publishEventAndWaitForResponse(Object event, String routingKey, String idempotencyKey, String requestId, int timeoutSeconds, RequestType requestType) throws Exception {
        log.info("Publishing event to routing key: {} with idempotency key: {} and request type: {} , request-id {}", routingKey, idempotencyKey, requestType,requestId);

        @SuppressWarnings("unchecked")
        CompletableFuture<T> existingFuture = (CompletableFuture<T>) pendingRequests.get(idempotencyKey);
        if (existingFuture != null) {
            log.info("Request with idempotency key {} is already being processed, waiting for completion", idempotencyKey);
            try {
                return waitForResponseWithErrorChecking(existingFuture, idempotencyKey, timeoutSeconds, requestType);
            } catch (Exception e) {
                log.error("Error waiting for existing request: {}", idempotencyKey, e);
                throw e;
            }
        }

        try {
            CompletableFuture<T> future = new CompletableFuture<>();
            pendingRequests.put(idempotencyKey, future);

            String messageBody = objectMapper.writeValueAsString(event);
            log.info("Publishing event payload: {}", messageBody);

            MessageProperties props = new MessageProperties();
            props.setContentType("application/json");
            props.setHeader("idempotency-key", idempotencyKey);
            props.setHeader("timestamp", Instant.now().toString());
            props.setHeader("request-type", requestType.getCode());
            props.setHeader("request-id", requestId);

            Message message = new Message(messageBody.getBytes(StandardCharsets.UTF_8), props);

            amqpTemplate.send(RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(), routingKey, message);

            log.info("Successfully published event for processing: {}", idempotencyKey);
            log.info("Waiting for consumer to process request: {}", idempotencyKey);

            // Wait for response with periodic error checking
            T result = waitForResponseWithErrorChecking(future, idempotencyKey, timeoutSeconds, requestType);
            log.info("Received result from consumer for request: {}", idempotencyKey);
            return result;

        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish event: JSON serialization error");
        } catch (TimeoutException e) {
            log.error("Timeout waiting for consumer to process request: {}", idempotencyKey, e);
            throw new RuntimeException("Request timed out - consumer may not be running");
        } catch (Exception e) {
            log.error("Error publishing event: {}", idempotencyKey, e);
            throw new RuntimeException("Request failed: " + e.getMessage());
        } finally {
            pendingRequests.remove(idempotencyKey);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void completeRequest(String idempotencyKey, T result) {
        CompletableFuture<T> future = (CompletableFuture<T>) pendingRequests.get(idempotencyKey);
        if (future != null) {
            future.complete(result);
            log.info("Completed pending request with idempotency key: {}", idempotencyKey);
        } else {
            log.warn("No pending request found for idempotency key: {}", idempotencyKey);
        }
    }

    @SuppressWarnings("unchecked")
    public void completeRequestWithError(String idempotencyKey, Exception exception) {
        CompletableFuture<?> future = pendingRequests.get(idempotencyKey);
        if (future != null) {
            future.completeExceptionally(exception);
            log.info("Completed pending request with error for idempotency key: {}", idempotencyKey);
        } else {
            log.warn("No pending request found for idempotency key: {}", idempotencyKey);
        }
    }

    public void removePendingRequest(String idempotencyKey) {
        pendingRequests.remove(idempotencyKey);
    }

    /**
     * Wait for response while periodically checking for errors from the error topic
     */
    @SuppressWarnings("unchecked")
    private <T> T waitForResponseWithErrorChecking(CompletableFuture<T> future, String idempotencyKey, int timeoutSeconds, RequestType requestType) throws Exception {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;
        long checkInterval = 50; // Check for errors more frequently (every 50ms)

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            // Check if the future is already completed
            if (future.isDone()) {
                return future.get();
            }

            // Check for errors from both error topics since we can't determine from RequestType
            ErrorNotification error = null;
            
            // Check account errors first
            error = accountNotificationConsumer.getErrorForIdempotencyKey(idempotencyKey);
            if (error != null) {
                accountNotificationConsumer.removeErrorFromCache(idempotencyKey);
            } else {
                // Check transaction errors if no account error found
                error = transactionNotificationConsumer.getErrorForIdempotencyKey(idempotencyKey);
                if (error != null) {
                    transactionNotificationConsumer.removeErrorFromCache(idempotencyKey);
                }
            }

            if (error != null) {
                log.error("Error detected for idempotency key {}: {} - {}", idempotencyKey, error.getErrorCode(), error.getErrorMessage());
                
                // Complete the future with an exception
                BusinessException businessException = new BusinessException(
                    error.getErrorMessage(), 
                    error.getErrorCode().getCode(), 
                    error.getErrorCode().getHttpStatus()
                );
                future.completeExceptionally(businessException);
                
                // Return the exception immediately
                throw businessException;
            } else {
                // Debug logging to see if we're checking but not finding errors
                if (System.currentTimeMillis() % 1000 < 50) { // Log every ~1 second
                    log.debug("No error found for idempotency key: {} (checking...)", idempotencyKey);
                }
            }

            // Wait a short interval before checking again
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request was interrupted", e);
            }
        }

        // Only timeout if no error was detected and no response received
        // This means the consumer is truly down or not processing
        log.error("Timeout waiting for response or error for idempotency key: {} - consumer may be down", idempotencyKey);
        throw new RuntimeException("Request timed out - consumer may not be running");
    }
} 