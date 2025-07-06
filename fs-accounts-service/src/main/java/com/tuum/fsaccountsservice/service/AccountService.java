package com.tuum.fsaccountsservice.service;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.dto.mq.CreateAccountEvent;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.types.RabbitMQConfig;
import com.tuum.common.types.RequestType;
import com.tuum.common.dto.mq.MQNotification;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.mapper.BalanceMapper;

import com.tuum.common.util.TraceIdGenerator;
import com.tuum.fsaccountsservice.util.DtoMapper;
import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.common.types.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final EventPublisherService eventPublisherService;
    private final TraceIdGenerator traceIdGenerator;
    private final IdempotencyService idempotencyService;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String idempotencyKey) {
        String requestId = traceIdGenerator.generateTraceId();
        log.info("Creating account with requestId: {} and idempotency key: {}", requestId, idempotencyKey);
        
        if (idempotencyService.isProcessed(idempotencyKey)) {
            log.info("Account creation already processed in memory, skipping: {}", idempotencyKey);
            // Return existing account from database if available
            Account existingAccount = accountMapper.findAccountByIdempotencyKey(idempotencyKey);
            if (existingAccount != null) {
                List<Balance> balances = balanceMapper.findBalancesByAccountId(existingAccount.getAccountId());
                existingAccount.setBalances(balances);
                return DtoMapper.toAccountResponse(existingAccount);
            }
            throw new BusinessException("Account creation already processed but not found in database");
        }
        
        idempotencyService.markAsProcessed(idempotencyKey);
        
        try {

            CreateAccountEvent event = new CreateAccountEvent();
            event.setRequestId(requestId);
            event.setCustomerId(request.getCustomerId());
            event.setCountry(request.getCountry());
            event.setIdempotencyKey(idempotencyKey);
            event.setCreatedAt(LocalDateTime.now());
            
            // Convert currencies to balances
            List<Balance> balances = request.getCurrencies().stream()
                .map(currency -> {
                    Balance balance = new Balance();
                    balance.setCurrency(currency);
                    balance.setAvailableAmount(BigDecimal.ZERO);
                    balance.setVersionNumber(1);
                    balance.setCreatedAt(LocalDateTime.now());
                    balance.setUpdatedAt(LocalDateTime.now());
                    return balance;
                })
                .collect(java.util.stream.Collectors.toList());
            event.setBalances(balances);

            log.info("Event: {}", event);
            log.info("Exchange: {}", RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue());
            log.info("Routing key: {}", RabbitMQConfig.ACCOUNTS_CREATED_ROUTING_KEY.getValue());

            return eventPublisherService.publishEventAndWaitForResponse(
                event, 
                RabbitMQConfig.ACCOUNTS_CREATED_ROUTING_KEY.getValue(),
                idempotencyKey,
                    requestId,
                30,
                 RequestType.CREATE
            );
            

        } catch (Exception e) {
            log.error("Error creating account: {}", idempotencyKey, e);
            throw new BusinessException("Failed to create account: " + e.getMessage());
        } finally {
            traceIdGenerator.clear();
        }
    }


    public void completeAccount(Object event, MQMessageData messageData) {
        String idempotencyKey = messageData.getIdempotencyKey();
        String status = messageData.getStatus();

        if ("SUCCESS".equalsIgnoreCase(status) && event instanceof CreateAccountEvent accountEvent) {
            AccountResponse response = new AccountResponse();
            response.setAccountId(accountEvent.getAccountId());
            response.setCustomerId(accountEvent.getCustomerId());
            response.setCountry(accountEvent.getCountry());
            response.setBalances(DtoMapper.toBalanceResponses(accountEvent.getBalances()));
            eventPublisherService.completeRequest(idempotencyKey, response);
        } else
            eventPublisherService.completeRequest(idempotencyKey, event);
    }


    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        Account account = accountMapper.findAccountById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        return account;
    }

    @Transactional(readOnly = true)
    public List<Balance> getAccountBalances(String accountId) {
        return balanceMapper.findBalancesByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountMapper.findAllAccounts();
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomerId(String customerId) {
        return accountMapper.findAccountsByCustomerId(customerId); // Uses JOIN
    }
} 