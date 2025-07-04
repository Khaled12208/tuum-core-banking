package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.csaccountseventsconsumer.dto.TransactionCreatedEvent;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import com.tuum.csaccountseventsconsumer.mapper.TransactionMapper;
import com.tuum.csaccountseventsconsumer.model.Balance;
import com.tuum.csaccountseventsconsumer.model.ProcessedMessage;
import com.tuum.csaccountseventsconsumer.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventService {

    private final TransactionMapper transactionMapper;
    private final BalanceMapper balanceMapper;
    private final ProcessedMessageMapper processedMessageMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final AmqpTemplate amqpTemplate;

    @Transactional
    public void processTransactionCreatedEvent(String message) {
        try {
            log.info("Processing transaction created event: {}", message);
            
            // Parse the event
            TransactionCreatedEvent event = objectMapper.readValue(message, TransactionCreatedEvent.class);
            
            // Check if already processed using idempotency key
            String messageId = event.getIdempotencyKey();
            if (processedMessageMapper.existsProcessedMessage(messageId)) {
                log.info("Transaction event already processed with idempotency key: {}", messageId);
                // Get the existing transaction to return the same response
                Transaction existingTransaction = transactionMapper.findTransactionByIdempotencyKey(event.getIdempotencyKey());
                if (existingTransaction != null) {
                    publishTransactionSuccessNotification(event, existingTransaction.getBalanceAfter(), message);
                }
                return;
            }
            
            // Check if transaction already exists using idempotency key
            if (transactionMapper.existsTransactionByIdempotencyKey(event.getIdempotencyKey())) {
                log.info("Transaction already exists with idempotency key: {}", event.getIdempotencyKey());
                // Get the existing transaction to return the same response
                Transaction existingTransaction = transactionMapper.findTransactionByIdempotencyKey(event.getIdempotencyKey());
                if (existingTransaction != null) {
                    publishTransactionSuccessNotification(event, existingTransaction.getBalanceAfter(), message);
                }
                return;
            }
            
            // Get current balance for the account and currency
            Balance currentBalance = balanceMapper.findBalanceByAccountIdAndCurrency(
                event.getAccountId(), event.getCurrency());
            
            if (currentBalance == null) {
                String errorMsg = "No " + event.getCurrency() + " balance found for account " + event.getAccountId();
                log.error(errorMsg);
                publishTransactionErrorNotification(event, errorMsg, message);
                return;
            }
            
            // Calculate new balance
            BigDecimal newBalance;
            if ("IN".equals(event.getDirection())) {
                newBalance = currentBalance.getAvailableAmount().add(event.getAmount());
            } else if ("OUT".equals(event.getDirection())) {
                // Check for insufficient funds
                if (currentBalance.getAvailableAmount().compareTo(event.getAmount()) < 0) {
                    String errorMsg = "Insufficient " + event.getCurrency() + " funds. Available: " + 
                                    currentBalance.getAvailableAmount() + " " + event.getCurrency() + 
                                    ", Required: " + event.getAmount() + " " + event.getCurrency();
                    log.error(errorMsg);
                    publishTransactionErrorNotification(event, errorMsg, message);
                    return;
                }
                newBalance = currentBalance.getAvailableAmount().subtract(event.getAmount());
            } else {
                String errorMsg = "Invalid transaction direction: " + event.getDirection() + ". Must be 'IN' or 'OUT'";
                log.error(errorMsg);
                publishTransactionErrorNotification(event, errorMsg, message);
                return;
            }
            
            // Update balance
            currentBalance.setAvailableAmount(newBalance);
            currentBalance.setVersionNumber(currentBalance.getVersionNumber() + 1);
            currentBalance.setUpdatedAt(LocalDateTime.now());
            balanceMapper.updateBalance(currentBalance);
            
            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionId(event.getTransactionId());
            transaction.setAccountId(event.getAccountId());
            transaction.setAmount(event.getAmount());
            transaction.setCurrency(event.getCurrency());
            transaction.setDirection(event.getDirection());
            transaction.setDescription(event.getDescription());
            transaction.setBalanceAfter(newBalance);
            transaction.setStatus("COMPLETED");
            transaction.setIdempotencyKey(event.getIdempotencyKey());
            transaction.setCreatedAt(LocalDateTime.now());
            
            transactionMapper.insertTransaction(transaction);
            log.info("Successfully created transaction: {} with new balance: {}", 
                    event.getTransactionId(), newBalance);
            
            // Record processed message
            ProcessedMessage processedMessage = new ProcessedMessage();
            processedMessage.setMessageId(messageId);
            processedMessage.setMessageType("CREATE_TRANSACTION");
            processedMessage.setProcessedAt(LocalDateTime.now());
            processedMessage.setResultData("{\"status\":\"SUCCESS\",\"transactionId\":\"" + event.getTransactionId() + "\"}");
            
            processedMessageMapper.insertProcessedMessage(processedMessage);
            
            // Publish success notification with all required fields
            publishTransactionSuccessNotification(event, newBalance, message);
            
        } catch (Exception e) {
            log.error("Error processing transaction created event: {}", message, e);
            try {
                TransactionCreatedEvent event = objectMapper.readValue(message, TransactionCreatedEvent.class);
                publishTransactionErrorNotification(event, e.getMessage(), message);
            } catch (Exception ex) {
                log.error("Failed to publish transaction error notification", ex);
            }
        }
    }
    
    private void publishTransactionSuccessNotification(TransactionCreatedEvent event, BigDecimal balanceAfterTransaction, String originalMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("transactionId", event.getTransactionId());
            notification.put("accountId", event.getAccountId());
            notification.put("amount", event.getAmount());
            notification.put("currency", event.getCurrency());
            notification.put("direction", event.getDirection());
            notification.put("description", event.getDescription());
            notification.put("idempotencyKey", event.getIdempotencyKey());
            notification.put("balanceAfterTransaction", balanceAfterTransaction);
            notification.put("status", "COMPLETED");
            notification.put("processedAt", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(notification);
            
            // Publish directly to the transactions notifications queue
            amqpTemplate.convertAndSend(
                "tuum.banking", 
                "transactions.notifications.success", 
                message);
            
            log.info("Published detailed transaction success notification for transaction: {}", event.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish transaction success notification for transaction: {}", event.getTransactionId(), e);
        }
    }
    
    private void publishTransactionErrorNotification(TransactionCreatedEvent event, String errorMessage, String originalMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("transactionId", event.getTransactionId());
            notification.put("accountId", event.getAccountId());
            notification.put("amount", event.getAmount());
            notification.put("currency", event.getCurrency());
            notification.put("direction", event.getDirection());
            notification.put("description", event.getDescription());
            notification.put("idempotencyKey", event.getIdempotencyKey());
            notification.put("status", "ERROR");
            notification.put("errorMessage", errorMessage);
            notification.put("processedAt", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(notification);
            
            log.info("Publishing error notification: {}", message);
            
            // Publish directly to the transactions errors queue
            amqpTemplate.convertAndSend(
                "tuum.banking", 
                "transactions.errors.processing", 
                message);
            
            log.info("Published detailed transaction error notification for transaction: {}", event.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish transaction error notification for transaction: {}", event.getTransactionId(), e);
        }
    }
} 