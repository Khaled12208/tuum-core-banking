package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.csaccountseventsconsumer.dto.AccountCreatedEvent;
import com.tuum.csaccountseventsconsumer.mapper.AccountMapper;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import com.tuum.csaccountseventsconsumer.model.Account;
import com.tuum.csaccountseventsconsumer.model.Balance;
import com.tuum.csaccountseventsconsumer.model.ProcessedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final ProcessedMessageMapper processedMessageMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public void processAccountCreatedEvent(String message) {
        try {
            log.info("Processing account created event: {}", message);
            
            // Parse the event - the message might contain a type field, so we need to handle it properly
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(message);
            
            AccountCreatedEvent event;
            // If the message has a type field, extract the data part
            if (jsonNode.has("type")) {
                // The event data is the entire message, but we need to ignore the type field
                event = new AccountCreatedEvent();
                event.setAccountId(jsonNode.has("accountId") ? jsonNode.get("accountId").asText() : null);
                event.setRequestId(jsonNode.has("requestId") ? jsonNode.get("requestId").asText() : null);
                event.setCustomerId(jsonNode.get("customerId").asText());
                event.setCountry(jsonNode.get("country").asText());
                event.setCurrencies(objectMapper.convertValue(jsonNode.get("currencies"), 
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
                event.setCreatedAt(jsonNode.get("createdAt").asText());
            } else {
                // Direct deserialization if no type field
                event = objectMapper.treeToValue(jsonNode, AccountCreatedEvent.class);
            }
            
            // Generate account ID if not provided
            String accountId = event.getAccountId();
            if (accountId == null || accountId.isEmpty()) {
                accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                event.setAccountId(accountId);
                log.info("Generated account ID: {} for request: {}", accountId, event.getRequestId());
            }
            
            // Check if already processed using requestId if available, otherwise use accountId
            String messageId = event.getRequestId() != null ? event.getRequestId() : accountId;
            if (processedMessageMapper.existsProcessedMessage(messageId)) {
                log.info("Account event already processed: {}", messageId);
                // Fetch existing account and balances for notification
                Account existingAccount = accountMapper.findAccountById(accountId);
                List<Balance> existingBalances = balanceMapper.findBalancesByAccountId(accountId);
                notificationService.publishAccountSuccessWithDetails(existingAccount, existingBalances, message, event.getRequestId());
                return;
            }
            
            // Check if account already exists
            if (accountMapper.existsAccountById(accountId)) {
                log.info("Account already exists: {}", accountId);
                // Publish error notification instead of success
                notificationService.publishAccountError(
                    accountId,
                    "Account already exists with id: " + accountId,
                    message,
                    event.getRequestId()
                );
                return;
            }
            
            // Create account
            Account account = new Account();
            account.setAccountId(accountId);
            account.setCustomerId(event.getCustomerId());
            account.setCountry(event.getCountry());
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            
            accountMapper.insertAccount(account);
            log.info("Successfully created account: {}", accountId);
            
            // Create balances for each currency
            List<Balance> balances = new java.util.ArrayList<>();
            for (String currency : event.getCurrencies()) {
                Balance balance = new Balance();
                balance.setBalanceId(UUID.randomUUID().toString());
                balance.setAccountId(accountId);
                balance.setCurrency(currency);
                balance.setAvailableAmount(BigDecimal.ZERO);
                balance.setVersionNumber(1);
                balance.setCreatedAt(LocalDateTime.now());
                balance.setUpdatedAt(LocalDateTime.now());
                
                balanceMapper.insertBalance(balance);
                balances.add(balance);
                log.info("Created balance for account {} with currency {}", accountId, currency);
            }
            
            // Record processed message
            ProcessedMessage processedMessage = new ProcessedMessage();
            processedMessage.setMessageId(messageId);
            processedMessage.setMessageType("CREATE_ACCOUNT");
            processedMessage.setProcessedAt(LocalDateTime.now());
            processedMessage.setResultData("{\"status\":\"SUCCESS\",\"accountId\":\"" + accountId + "\"}");
            
            processedMessageMapper.insertProcessedMessage(processedMessage);
            
            // Publish success notification with detailed account information
            notificationService.publishAccountSuccessWithDetails(account, balances, message, event.getRequestId());
            
        } catch (Exception e) {
            log.error("Error processing account created event: {}", message, e);
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(message);
                String accountId = jsonNode.has("accountId") ? jsonNode.get("accountId").asText() : "UNKNOWN";
                String requestId = jsonNode.has("requestId") ? jsonNode.get("requestId").asText() : null;
                notificationService.publishAccountError(accountId, message, e.getMessage(), requestId);
            } catch (Exception ex) {
                log.error("Failed to publish account error notification", ex);
            }
        }
    }
} 