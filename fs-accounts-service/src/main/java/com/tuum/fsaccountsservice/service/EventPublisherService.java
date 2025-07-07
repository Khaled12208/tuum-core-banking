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
import java.util.concurrent.*;
import com.tuum.common.exception.InsufficientFundsException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;
    private final AccountNotificationConsumer accountNotificationConsumer;
    private final TransactionNotificationConsumer transactionNotificationConsumer;

    private final ConcurrentHashMap<String, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService errorCheckerScheduler = Executors.newScheduledThreadPool(2);

    public <T> T publishEventAndWaitForResponse(Object event, String routingKey, String idempotencyKey, String requestId, int timeoutSeconds, RequestType requestType) throws InsufficientFundsException, BusinessException {
        log.info("Publishing event to routing key: {} with idempotency key: {} and request type: {} , request-id {}", routingKey, idempotencyKey, requestType,requestId);

        @SuppressWarnings("unchecked")
        CompletableFuture<T> existingFuture = (CompletableFuture<T>) pendingRequests.get(idempotencyKey);
        if (existingFuture != null) {
            log.info("Request with idempotency key {} is already being processed, waiting for completion", idempotencyKey);
            try {
                return waitForResponseWithErrorChecking(existingFuture, idempotencyKey, timeoutSeconds, requestType);
            } catch (InsufficientFundsException e) {
                log.error("InsufficientFundsException waiting for existing request: {}", idempotencyKey, e);
                throw e;
            } catch (BusinessException e) {
                log.error("BusinessException waiting for existing request: {}", idempotencyKey, e);
                throw e;
            } catch (Exception e) {
                log.error("Error waiting for existing request: {}", idempotencyKey, e);
                throw new BusinessException("Request failed: " + e.getMessage());
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

            T result = waitForResponseWithErrorChecking(future, idempotencyKey, timeoutSeconds, requestType);
            log.info("Received result from consumer for request: {}", idempotencyKey);
            return result;

        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", e.getMessage(), e);
            throw new BusinessException("Failed to publish event: JSON serialization error");
        } catch (InsufficientFundsException e) {
            log.error("InsufficientFundsException caught in EventPublisherService: {}", idempotencyKey, e);
            throw e;
        } catch (BusinessException e) {
            log.error("BusinessException caught in EventPublisherService: {}", idempotencyKey, e);
            throw e;
        } catch (RuntimeException e) {
            log.error("RuntimeException caught in EventPublisherService: {} - Exception type: {}", idempotencyKey, e.getClass().getName(), e);
            throw new BusinessException("Request failed: " + e.getMessage());
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

    @SuppressWarnings("unchecked")
    private <T> T waitForResponseWithErrorChecking(CompletableFuture<T> future, String idempotencyKey, int timeoutSeconds, RequestType requestType) throws InsufficientFundsException, BusinessException {
        long timeoutMillis = timeoutSeconds * 1000L;
        
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        errorCheckerScheduler.schedule(() -> {
            if (!future.isDone()) {
                timeoutFuture.completeExceptionally(new java.util.concurrent.TimeoutException("Request timed out"));
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);
        
        ScheduledFuture<?> errorChecker = errorCheckerScheduler.scheduleAtFixedRate(() -> {
            if (future.isDone() || timeoutFuture.isDone()) {
                return; 
            }
            
            ErrorNotification error = null;
            error = accountNotificationConsumer.getErrorForIdempotencyKey(idempotencyKey);
            if (error != null) {
                accountNotificationConsumer.removeErrorFromCache(idempotencyKey);
            } else {
                error = transactionNotificationConsumer.getErrorForIdempotencyKey(idempotencyKey);
                if (error != null) {
                    transactionNotificationConsumer.removeErrorFromCache(idempotencyKey);
                }
            }
            
            if (error != null) {
                log.error("Error detected for idempotency key {}: {} - {}", idempotencyKey, error.getErrorCode(), error.getErrorMessage());
                
                if (error.getErrorCode() == ErrorCode.INSUFFICIENT_FUNDS) {
                    InsufficientFundsException insufficientFundsException = new InsufficientFundsException(error.getErrorMessage());
                    future.completeExceptionally(insufficientFundsException);
                } else {
                    BusinessException businessException = new BusinessException(
                        error.getErrorMessage(), 
                        error.getErrorCode().getCode(), 
                        error.getErrorCode().getHttpStatus()
                    );
                    future.completeExceptionally(businessException);
                }
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
        
        try {
            return CompletableFuture.anyOf(future, timeoutFuture).thenApply(result -> {
                if (result instanceof Throwable) {
                    throw new CompletionException((Throwable) result);
                }
                return (T) result;
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Request was interrupted");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.util.concurrent.TimeoutException) {
                log.error("Timeout waiting for response for idempotency key: {} - consumer may be down", idempotencyKey);
                throw new BusinessException("Request timed out - consumer may not be running");
            } else if (cause instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) cause;
            } else if (cause instanceof BusinessException) {
                throw (BusinessException) cause;
            } else if (e instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) e;
            } else if (e instanceof BusinessException) {
                throw (BusinessException) e;
            } else {
                throw new BusinessException("Request failed: " + e.getMessage());
            }
        } finally {
            errorChecker.cancel(false);
        }
    }
} 