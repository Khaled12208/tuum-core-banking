package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.csaccountseventsconsumer.config.RabbitMQConfig;
import com.tuum.csaccountseventsconsumer.model.Account;
import com.tuum.csaccountseventsconsumer.model.Balance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;

    public void publishAccountSuccess(String accountId, String originalMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("accountId", accountId);
            notification.put("status", "SUCCESS");
            notification.put("message", "Account processed successfully");
            notification.put("originalMessage", originalMessage);
            notification.put("processedAt", LocalDateTime.now().toString());
            notification.put("type", "ACCOUNT_PROCESSED");

            String message = objectMapper.writeValueAsString(notification);
            amqpTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.ACCOUNTS_SUCCESS_ROUTING_KEY, message);
            
            log.info("Published account success notification for account: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to publish account success notification for account: {}", accountId, e);
        }
    }

    public void publishAccountSuccessWithDetails(Account account, List<Balance> balances, String originalMessage, String requestId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("accountId", account.getAccountId());
            notification.put("customerId", account.getCustomerId());
            notification.put("country", account.getCountry());
            notification.put("status", "SUCCESS");
            notification.put("processedAt", LocalDateTime.now().toString());
            notification.put("type", "ACCOUNT_PROCESSED");
            notification.put("requestId", requestId);
            
            // Convert balances to the required format
            List<Map<String, Object>> balanceList = balances.stream()
                .map(balance -> {
                    Map<String, Object> balanceMap = new HashMap<>();
                    balanceMap.put("currency", balance.getCurrency());
                    balanceMap.put("availableAmount", balance.getAvailableAmount());
                    return balanceMap;
                })
                .collect(Collectors.toList());
            
            notification.put("balances", balanceList);

            String message = objectMapper.writeValueAsString(notification);
            amqpTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.ACCOUNTS_SUCCESS_ROUTING_KEY, message);
            
            log.info("Published detailed account success notification for account: {}", account.getAccountId());
        } catch (Exception e) {
            log.error("Failed to publish detailed account success notification for account: {}", account.getAccountId(), e);
        }
    }

    public void publishAccountError(String accountId, String errorMessage, String originalMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("accountId", accountId);
            notification.put("status", "ERROR");
            notification.put("errorMessage", errorMessage);
            notification.put("originalMessage", originalMessage);
            notification.put("processedAt", LocalDateTime.now().toString());
            notification.put("type", "ACCOUNT_ERROR");

            String message = objectMapper.writeValueAsString(notification);
            amqpTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.ACCOUNTS_ERROR_ROUTING_KEY, message);
            
            log.info("Published account error notification for account: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to publish account error notification for account: {}", accountId, e);
        }
    }

    public void publishAccountError(String accountId, String errorMessage, String originalMessage, String requestId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("accountId", accountId);
            notification.put("status", "ERROR");
            notification.put("errorMessage", errorMessage);
            notification.put("originalMessage", originalMessage);
            notification.put("processedAt", LocalDateTime.now().toString());
            notification.put("type", "ACCOUNT_ERROR");
            notification.put("requestId", requestId);

            String message = objectMapper.writeValueAsString(notification);
            amqpTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.ACCOUNTS_ERROR_ROUTING_KEY, message);
            
            log.info("Published account error notification for account: {} with requestId: {}", accountId, requestId);
        } catch (Exception e) {
            log.error("Failed to publish account error notification for account: {}", accountId, e);
        }
    }

    public void publishTransactionSuccess(String transactionId, String originalMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("transactionId", transactionId);
            notification.put("status", "SUCCESS");
            notification.put("message", "Transaction processed successfully");
            notification.put("originalMessage", originalMessage);
            notification.put("processedAt", LocalDateTime.now().toString());
            notification.put("type", "TRANSACTION_PROCESSED");

            String message = objectMapper.writeValueAsString(notification);
            amqpTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.TRANSACTIONS_SUCCESS_ROUTING_KEY, message);
            
            log.info("Published transaction success notification for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish transaction success notification for transaction: {}", transactionId, e);
        }
    }

    public void publishTransactionError(String transactionId, String errorMessage, String originalMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("transactionId", transactionId);
            notification.put("status", "ERROR");
            notification.put("errorMessage", errorMessage);
            notification.put("processedAt", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(notification);
            amqpTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.TRANSACTIONS_ERROR_ROUTING_KEY, message);
            
            log.info("Published transaction error notification for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish transaction error notification for transaction: {}", transactionId, e);
        }
    }
} 