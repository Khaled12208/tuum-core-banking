package com.tuum.fsaccountsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.fsaccountsservice.config.RabbitMQConfig;
import com.tuum.fsaccountsservice.dto.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.TransactionCreatedEvent;
import com.tuum.fsaccountsservice.dto.TransactionProcessedEvent;
import com.tuum.fsaccountsservice.exception.BusinessException;
import com.tuum.fsaccountsservice.exception.ValidationException;
import com.tuum.fsaccountsservice.exception.ErrorResponse;
import com.tuum.fsaccountsservice.mapper.TransactionMapper;
import com.tuum.fsaccountsservice.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionMapper transactionMapper;

    // Store pending transactions waiting for WebSocket response
    private final ConcurrentHashMap<String, CompletableFuture<TransactionProcessedEvent>> pendingTransactions = new ConcurrentHashMap<>();

    public TransactionProcessedEvent createTransaction(CreateTransactionRequest request, String idempotencyKey) {
        log.info("Creating transaction for account: {} with idempotency key: {}", request.getAccountId(), idempotencyKey);
        
        // Service-level validation for amount > 0
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(List.of(
                ErrorResponse.ValidationError.builder()
                    .field("amount")
                    .message("Transaction amount must be positive")
                    .rejectedValue(request.getAmount() != null ? request.getAmount().toString() : "null")
                    .build()
            ));
        }
        
        // Check if we already have a pending transaction with this idempotency key
        CompletableFuture<TransactionProcessedEvent> existingFuture = pendingTransactions.get(idempotencyKey);
        if (existingFuture != null) {
            log.info("Transaction with idempotency key {} is already being processed, waiting for completion", idempotencyKey);
            try {
                TransactionProcessedEvent result = existingFuture.get(30, TimeUnit.SECONDS);
                log.info("Returning existing transaction result for idempotency key: {}", idempotencyKey);
                return result;
            } catch (java.util.concurrent.TimeoutException e) {
                log.error("Timeout waiting for existing transaction with idempotency key: {}", idempotencyKey, e);
                throw new BusinessException("Transaction creation timed out - consumer may not be running");
            } catch (Exception e) {
                log.error("Error waiting for existing transaction with idempotency key: {}", idempotencyKey, e);
                throw new BusinessException("Transaction creation failed: " + e.getMessage());
            }
        }
        
        // Generate transaction ID
        String transactionId = UUID.randomUUID().toString();
        
        // Create transaction event
        TransactionCreatedEvent event = new TransactionCreatedEvent();
        event.setTransactionId(transactionId);
        event.setAccountId(request.getAccountId());
        event.setAmount(request.getAmount());
        event.setCurrency(request.getCurrency().toString());
        event.setDirection(request.getDirection().toString());
        event.setDescription(request.getDescription());
        event.setIdempotencyKey(idempotencyKey);
        event.setCreatedAt(LocalDateTime.now().toString());
        
        // Create a future to wait for completion and store it with idempotency key
        CompletableFuture<TransactionProcessedEvent> future = new CompletableFuture<>();
        pendingTransactions.put(idempotencyKey, future);
        
        try {
            log.info("About to publish transaction event to RabbitMQ: {}", event);
            
            // Publish transaction creation request to message queue
            rabbitTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                "transactions.events.created", event);
            
            log.info("Successfully published transaction creation request for consumer processing: {}", transactionId);
            
            // Wait for completion
            log.info("Waiting for consumer to process transaction: {}", transactionId);
            TransactionProcessedEvent result = future.get(30, TimeUnit.SECONDS);
            log.info("Received result from consumer for transaction: {}", transactionId);
            return result;
            
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout waiting for consumer to process transaction: {}", transactionId, e);
            throw new BusinessException("Transaction creation timed out - consumer may not be running");
        } catch (Exception e) {
            log.error("Error creating transaction: {}", transactionId, e);
            throw new BusinessException("Transaction creation failed: " + e.getMessage());
        } finally {
            pendingTransactions.remove(idempotencyKey);
        }
    }

    public void completeTransaction(String idempotencyKey, TransactionProcessedEvent event) {
        CompletableFuture<TransactionProcessedEvent> future = pendingTransactions.get(idempotencyKey);
        if (future != null) {
            future.complete(event);
            log.info("Completed pending transaction with idempotency key: {}", idempotencyKey);
        } else {
            log.warn("No pending transaction found for idempotency key: {}", idempotencyKey);
        }
    }

    public List<Transaction> getTransactionsByAccountId(String accountId) {
        log.info("Retrieving transactions for account: {}", accountId);
        List<Transaction> transactions = transactionMapper.findTransactionsByAccountId(accountId);
        log.info("Found {} transactions for account: {}", transactions != null ? transactions.size() : 0, accountId);
        return transactions;
    }

    public Transaction getTransactionById(String transactionId) {
        log.info("Retrieving transaction by ID: {}", transactionId);
        Transaction transaction = transactionMapper.findTransactionById(transactionId);
        if (transaction == null) {
            log.warn("Transaction not found with ID: {}", transactionId);
        } else {
            log.info("Found transaction: {}", transactionId);
        }
        return transaction;
    }
} 