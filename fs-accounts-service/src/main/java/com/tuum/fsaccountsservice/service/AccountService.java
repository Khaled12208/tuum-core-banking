package com.tuum.fsaccountsservice.service;

import com.tuum.fsaccountsservice.config.RabbitMQConfig;
import com.tuum.fsaccountsservice.dto.AccountCreatedEvent;
import com.tuum.fsaccountsservice.dto.AccountProcessedEvent;
import com.tuum.fsaccountsservice.dto.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.CreateAccountResponse;
import com.tuum.fsaccountsservice.exception.BusinessException;
import com.tuum.fsaccountsservice.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.exception.ValidationException;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.model.Account;
import com.tuum.fsaccountsservice.model.Currency;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountMapper accountMapper;
    private final RabbitTemplate rabbitTemplate;

    // Store pending accounts waiting for WebSocket response
    private final ConcurrentHashMap<String, CompletableFuture<AccountProcessedEvent>> pendingAccounts = new ConcurrentHashMap<>();
    
    // Store idempotency keys to prevent duplicate submissions
    private final ConcurrentHashMap<String, String> idempotencyKeys = new ConcurrentHashMap<>();
    
    // Store completed accounts for idempotency checks
    private final ConcurrentHashMap<String, AccountProcessedEvent> completedAccounts = new ConcurrentHashMap<>();

    /**
     * Creates an account asynchronously and waits for completion
     * Handles concurrent account creation and prevents duplicate submissions
     */
    public AccountProcessedEvent createAccount(CreateAccountRequest request, String idempotencyKey) {
        log.info("Creating account for customer: {} with idempotency key: {}", request.getCustomerId(), idempotencyKey);
        
        // Generate requestId for this attempt
        String requestId = "REQ" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Atomically check and set idempotency key
        String existingRequestId = idempotencyKeys.putIfAbsent(idempotencyKey, requestId);
        if (existingRequestId != null) {
            log.warn("Duplicate account creation detected with idempotency key: {}. Waiting for existing request: {}", 
                    idempotencyKey, existingRequestId);
            return waitForAccountCompletion(existingRequestId, 30);
        }
        
        // Validate currencies
        validateCurrencies(request.getCurrencies());
        
        // Create a future to wait for completion
        CompletableFuture<AccountProcessedEvent> future = new CompletableFuture<>();
        pendingAccounts.put(requestId, future);
        
        // Process account creation asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // Publish account created event to message queue (without accountId - consumer will generate it)
                publishAccountCreatedEvent(request, requestId);
                log.info("Published account created event for consumer processing (requestId: {})", requestId);
            } catch (Exception e) {
                log.error("Error processing account creation", e);
                AccountProcessedEvent errorEvent = createErrorEvent(null, e.getMessage(), requestId);
                completeAccount(requestId, errorEvent);
            }
        });
        
        // Wait for completion
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timeout waiting for account creation", e);
            throw new BusinessException("Account creation timeout");
        }
    }

    /**
     * Complete a pending account when WebSocket notification is received
     */
    public void completeAccount(String requestId, AccountProcessedEvent event) {
        CompletableFuture<AccountProcessedEvent> future = pendingAccounts.get(requestId);
        if (future != null) {
            future.complete(event);
            pendingAccounts.remove(requestId);
            completedAccounts.put(requestId, event);
            log.info("Completed pending account for requestId: {}", requestId);
        }
    }

    /**
     * Wait for an existing account to complete
     */
    private AccountProcessedEvent waitForAccountCompletion(String requestId, int timeoutSeconds) {
        // First check if already completed
        AccountProcessedEvent completedEvent = completedAccounts.get(requestId);
        if (completedEvent != null) {
            log.info("Returning completed account for idempotency key: {}", requestId);
            return completedEvent;
        }
        
        // Wait for pending account if present
        CompletableFuture<AccountProcessedEvent> future = pendingAccounts.get(requestId);
        if (future != null) {
            try {
                AccountProcessedEvent result = future.get(timeoutSeconds, TimeUnit.SECONDS);
                log.info("Waited and completed account for idempotency: {}", requestId);
                return result;
            } catch (Exception e) {
                log.error("Error waiting for account completion", e);
                throw new BusinessException("Failed to wait for account completion: " + requestId);
            }
        }
        
        throw new BusinessException("Account not found in pending or completed accounts: " + requestId);
    }

    public Account getAccount(String accountId) {
        log.info("Getting account: {}", accountId);
        
        Account account = accountMapper.findAccountById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        
        return account;
    }

    public List<Account> getAccountsByCustomerId(String customerId) {
        log.info("Getting accounts for customer: {}", customerId);
        
        List<Account> accounts = accountMapper.findAccountsByCustomerId(customerId);
        
        return accounts;
    }

    public List<Account> getAllAccounts() {
        log.info("Getting all accounts");
        
        List<Account> accounts = accountMapper.findAllAccounts();
        
        return accounts;
    }

    public List<Account> getAccountsByCurrencyAndAccountId(String currency, String accountId) {
        // TODO: Implement filtering logic
        return List.of();
    }

    private void validateCurrencies(List<Currency> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            throw new ValidationException("At least one currency must be specified");
        }
        
        for (Currency currency : currencies) {
            if (currency == null) {
                throw new ValidationException("Currency cannot be null");
            }
        }
    }

    private void publishAccountCreatedEvent(CreateAccountRequest request, String requestId) {
        try {
            String idempotencyKey = "account_created_" + request.getCustomerId();
            
            // Create event payload with account and currencies
            AccountCreatedEvent event = new AccountCreatedEvent();
            event.setRequestId(requestId);
            event.setCustomerId(request.getCustomerId());
            event.setCountry(request.getCountry());
            event.setCurrencies(request.getCurrencies().stream().map(Currency::name).collect(Collectors.toList()));
            event.setCreatedAt(LocalDateTime.now().toString());
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, RabbitMQConfig.ACCOUNT_CREATED_ROUTING_KEY, event, message -> {
                message.getMessageProperties().setHeader("idempotency-key", idempotencyKey);
                return message;
            });
            log.info("Published account created event for account: {} with currencies: {} and requestId: {}", 
                    request.getCustomerId(), request.getCurrencies(), requestId);
        } catch (Exception e) {
            log.error("Failed to publish account created event", e);
            throw new BusinessException("Failed to publish account created event: " + e.getMessage());
        }
    }

    private void publishAccountProcessedEvent(AccountProcessedEvent event) {
        try {
            // Publish to tuum.banking exchange with processed routing key
            rabbitTemplate.convertAndSend(RabbitMQConfig.TUUM_BANKING_EXCHANGE, 
                RabbitMQConfig.ACCOUNT_PROCESSED_ROUTING_KEY, event);
        } catch (Exception e) {
            log.error("Failed to publish account processed event", e);
            throw new BusinessException("Failed to publish account processed event");
        }
    }

    private AccountProcessedEvent createAccountProcessedEvent(Account account, List<Currency> currencies) {
        AccountProcessedEvent event = new AccountProcessedEvent();
        event.setAccountId(account.getAccountId());
        event.setCustomerId(account.getCustomerId());
        event.setCountry(account.getCountry());
        event.setCurrencies(currencies.stream().map(Currency::name).collect(Collectors.toList()));
        event.setStatus("COMPLETED");
        event.setErrorMessage(null);
        event.setProcessedAt(LocalDateTime.now().toString());
        
        // Create initial balances (0.0 for each currency)
        List<AccountProcessedEvent.BalanceInfo> balances = currencies.stream()
            .map(currency -> {
                AccountProcessedEvent.BalanceInfo balance = new AccountProcessedEvent.BalanceInfo();
                balance.setCurrency(currency.name());
                balance.setAvailableAmount(BigDecimal.ZERO);
                return balance;
            })
            .collect(Collectors.toList());
        event.setBalances(balances);
        
        return event;
    }

    private AccountProcessedEvent createErrorEvent(Account account, String errorMessage, String requestId) {
        AccountProcessedEvent event = new AccountProcessedEvent();
        event.setRequestId(requestId);
        event.setStatus("FAILED");
        event.setErrorMessage(errorMessage);
        event.setProcessedAt(LocalDateTime.now().toString());
        return event;
    }
} 