package com.tuum.fsaccountsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.fsaccountsservice.dto.AccountProcessedEvent;
import com.tuum.fsaccountsservice.dto.TransactionProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final AccountService accountService;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate, 
                                      ObjectMapper objectMapper,
                                      TransactionService transactionService,
                                      AccountService accountService) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    /**
     * Consumes successful transaction notifications and sends WebSocket notifications
     */
    @RabbitListener(queues = "transactions-notifications-queue")
    public void handleTransactionNotification(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.info("Received transaction notification: {}", messageBody);
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            log.info("Message content type: {}", message.getMessageProperties().getContentType());
            
            TransactionProcessedEvent event = objectMapper.readValue(messageBody, TransactionProcessedEvent.class);
            
            // Complete the pending transaction using idempotency key
            transactionService.completeTransaction(event.getIdempotencyKey(), event);
            
            // Send detailed success notification via WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "TRANSACTION_SUCCESS");
            notification.put("accountId", event.getAccountId());
            notification.put("transactionId", event.getTransactionId());
            notification.put("amount", event.getAmount());
            notification.put("currency", event.getCurrency());
            notification.put("direction", event.getDirection());
            notification.put("description", event.getDescription());
            notification.put("balanceAfterTransaction", event.getBalanceAfterTransaction());
            notification.put("status", event.getStatus());
            notification.put("processedAt", event.getProcessedAt());
            notification.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/transactions", notification);
            log.info("Sent detailed transaction success notification: {}", event.getTransactionId());
            
        } catch (Exception e) {
            log.error("Error processing transaction notification: {}", e.getMessage(), e);
            log.error("Failed to deserialize message: {}", new String(message.getBody()));
        }
    }

    /**
     * Consumes failed transactions from error queue and sends WebSocket notifications
     */
    @RabbitListener(queues = "transactions-errors-queue")
    public void handleTransactionError(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.warn("Received failed transaction from error queue: {}", messageBody);
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            log.info("Message content type: {}", message.getMessageProperties().getContentType());
            
            TransactionProcessedEvent event = objectMapper.readValue(messageBody, TransactionProcessedEvent.class);
            
            // Complete the pending transaction with error using idempotency key
            transactionService.completeTransaction(event.getIdempotencyKey(), event);
            
            // Send error notification via WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "TRANSACTION_ERROR");
            notification.put("transactionId", event.getTransactionId());
            notification.put("accountId", event.getAccountId());
            notification.put("errorMessage", event.getErrorMessage());
            notification.put("timestamp", event.getProcessedAt());
            
            messagingTemplate.convertAndSend("/topic/transactions", notification);
            log.info("Sent transaction error notification: {}", event.getTransactionId());
            
        } catch (Exception e) {
            log.error("Error processing transaction error message: {}", e.getMessage(), e);
            log.error("Failed to deserialize message: {}", new String(message.getBody()));
        }
    }

    /**
     * Consumes successful account notifications and sends WebSocket notifications
     */
    @RabbitListener(queues = "accounts-notifications-queue")
    public void handleAccountNotification(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.info("Received account notification: {}", messageBody);
            
            AccountProcessedEvent event = objectMapper.readValue(messageBody, AccountProcessedEvent.class);
            
            // Complete the pending account using requestId
            accountService.completeAccount(event.getRequestId(), event);
            
            // Send detailed success notification via WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ACCOUNT_SUCCESS");
            notification.put("accountId", event.getAccountId());
            notification.put("customerId", event.getCustomerId());
            notification.put("country", event.getCountry());
            notification.put("currencies", event.getCurrencies());
            notification.put("balances", event.getBalances());
            notification.put("status", event.getStatus());
            notification.put("processedAt", event.getProcessedAt());
            notification.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/accounts", notification);
            log.info("Sent detailed account success notification: {}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Error processing account notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes failed accounts from error queue and sends WebSocket notifications
     */
    @RabbitListener(queues = "accounts-errors-queue")
    public void handleAccountError(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.warn("Received failed account from error queue: {}", messageBody);
            
            AccountProcessedEvent event = objectMapper.readValue(messageBody, AccountProcessedEvent.class);
            
            // Complete the pending account with error
            accountService.completeAccount(event.getAccountId(), event);
            
            // Send error notification via WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ACCOUNT_ERROR");
            notification.put("accountId", event.getAccountId());
            notification.put("customerId", event.getCustomerId());
            notification.put("errorMessage", event.getErrorMessage());
            notification.put("timestamp", event.getProcessedAt());
            
            messagingTemplate.convertAndSend("/topic/accounts", notification);
            log.info("Sent account error notification: {}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Error processing account error message: {}", e.getMessage(), e);
        }
    }
} 