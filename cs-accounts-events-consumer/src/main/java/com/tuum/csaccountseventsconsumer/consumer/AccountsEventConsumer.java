package com.tuum.csaccountseventsconsumer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import com.tuum.csaccountseventsconsumer.model.Account;
import com.tuum.csaccountseventsconsumer.model.Balance;
import com.tuum.csaccountseventsconsumer.dto.AccountCreatedEvent;
import com.tuum.csaccountseventsconsumer.model.ProcessedMessage;
import com.tuum.csaccountseventsconsumer.mapper.AccountMapper;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import com.tuum.csaccountseventsconsumer.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AccountsEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(AccountsEventConsumer.class);
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final ProcessedMessageMapper processedMessageMapper;

    public AccountsEventConsumer(NotificationService notificationService, 
                                ObjectMapper objectMapper,
                                AccountMapper accountMapper,
                                BalanceMapper balanceMapper,
                                ProcessedMessageMapper processedMessageMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.accountMapper = accountMapper;
        this.balanceMapper = balanceMapper;
        this.processedMessageMapper = processedMessageMapper;
    }

    @RabbitListener(queues = "accounts-events-queue")
    public void handleAccountCreatedEvent(String message) {
        log.info("Received account creation request: {}", message);
        
        try {
            // Parse the account creation request
            AccountCreatedEvent event = objectMapper.readValue(message, AccountCreatedEvent.class);
            
            // Generate account ID (UUID generation moved to consumer side)
            String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            event.setAccountId(accountId);
            
            log.info("Processing account creation for customer: {} with generated account ID: {}", 
                    event.getCustomerId(), accountId);
            
            // Check if account already exists for this customer (optional business rule)
            // For now, we'll allow multiple accounts per customer
            
            // Create account in database
            Account account = new Account();
            account.setAccountId(accountId);
            account.setCustomerId(event.getCustomerId());
            account.setCountry(event.getCountry());
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            
            accountMapper.insertAccount(account);
            log.info("Created account in database: {}", accountId);
            
            // Create balances for each currency
            List<Balance> balances = new ArrayList<>();
            for (String currency : event.getCurrencies()) {
                Balance balance = new Balance();
                balance.setBalanceId("BAL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                balance.setAccountId(accountId);
                balance.setCurrency(currency);
                balance.setAvailableAmount(BigDecimal.ZERO);
                balance.setVersionNumber(1);
                balance.setCreatedAt(LocalDateTime.now());
                balance.setUpdatedAt(LocalDateTime.now());
                
                balanceMapper.insertBalance(balance);
                balances.add(balance);
                log.info("Created balance for account: {} currency: {}", accountId, currency);
            }
            
            // Create processed message record
            ProcessedMessage processedMessage = new ProcessedMessage();
            processedMessage.setMessageId(UUID.randomUUID().toString());
            processedMessage.setMessageType("CREATE_ACCOUNT");
            processedMessage.setProcessedAt(LocalDateTime.now());
            processedMessage.setResultData("{\"status\":\"SUCCESS\",\"accountId\":\"" + accountId + "\"}");
            
            processedMessageMapper.insertProcessedMessage(processedMessage);
            
            // Publish success notification with the generated account ID
            publishAccountSuccessNotification(event, account, balances, event.getRequestId());
            
            log.info("Successfully processed account creation for account: {}", accountId);
            
        } catch (Exception e) {
            log.error("Error processing account creation request", e);
            
            try {
                // Try to parse the original event to get customer info for error response
                AccountCreatedEvent originalEvent = objectMapper.readValue(message, AccountCreatedEvent.class);
                publishAccountErrorNotification(originalEvent, e.getMessage());
            } catch (Exception parseError) {
                log.error("Failed to parse original event for error notification", parseError);
            }
        }
    }
    
    private void publishAccountSuccessNotification(AccountCreatedEvent event, Account account, List<Balance> balances, String requestId) {
        notificationService.publishAccountSuccessWithDetails(account, balances, event.toString(), requestId);
    }
    
    private void publishAccountErrorNotification(AccountCreatedEvent event, String errorMessage) {
        String accountId = event.getAccountId() != null ? event.getAccountId() : "UNKNOWN";
        notificationService.publishAccountError(accountId, errorMessage, event.toString());
    }
} 