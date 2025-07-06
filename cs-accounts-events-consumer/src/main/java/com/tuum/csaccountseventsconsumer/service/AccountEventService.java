package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.domain.entities.ProcessedMessage;
import com.tuum.common.dto.mq.CreateAccountEvent;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.exception.ProcessingException;
import com.tuum.common.types.ErrorCode;
import com.tuum.common.types.RabbitMQConfig;
import com.tuum.common.types.RequestType;
import com.tuum.csaccountseventsconsumer.mapper.AccountMapper;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final ProcessedMessageMapper processedMessageMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public void processAccountEvent(MQMessageData message) {
        try {
            CreateAccountEvent event = objectMapper.readValue(message.getMessageBody(), CreateAccountEvent.class);
            ensureAccountId(event);

            switch (message.getRequestType()) {
                case CREATE:
                    handleCreate(event, message);
                    break;
                default:
                    log.warn("Unsupported request type: {}", message.getRequestType());
                    throw new ProcessingException(
                            "Account creation failed due to unsupported request type",
                            RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                            RabbitMQConfig.ACCOUNTS_ERROR_ROUTING_KEY.getValue(),
                            "ACCOUNT_CREATION_ERROR",
                            event.getRequestId(),
                            null
                    );
            }
        } catch (Exception e) {
            log.error("Error processing account event: {}, request-id: {} ", e.getMessage(), message.getRequestId(), e);
            notificationService.publishErrorResponse(RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(), RabbitMQConfig.ACCOUNTS_ERROR_ROUTING_KEY.getValue(), message, ErrorCode.ACCOUNT_CREATION_FAILED, e.getMessage());
        }
    }

    private void handleCreate(CreateAccountEvent event, MQMessageData message) {
        String messageId = Optional.ofNullable(event.getRequestId()).orElse(event.getAccountId());
        
        if (processedMessageMapper.existsProcessedMessage(messageId)) {
            log.info("Event already processed in database: {}", messageId);
            publishSuccessNotificationForExistingAccount(event.getAccountId(), message.getMessageBody(), event, message.getRequestType());
            return;
        }

        if (accountMapper.existsAccountById(event.getAccountId())) {
            log.warn("Account already exists: {}", event.getAccountId());
            throw new ProcessingException(
                    "Account creation failed due to Account already exists",
                    RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                    RabbitMQConfig.ACCOUNTS_ERROR_ROUTING_KEY.getValue(),
                    "ACCOUNT_CREATION_ERROR",
                    event.getRequestId(),
                    null
            );
        }

        Account account = createAndSaveAccount(event);
        List<Balance> balances = createAndSaveBalances(account.getAccountId(), event.getBalances());
        account.setBalances(balances);
        
        recordProcessedMessage(messageId, account.getAccountId(), event.getIdempotencyKey());
        
        notificationService.publishSuccessNotification(
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.ACCOUNTS_PROCESSED_ROUTING_KEY.getValue(),
                message.getRequestType().getCode(),
                "SUCCESS",
                event.getRequestId(),
                account,
                event.getIdempotencyKey(),
                null
        );
        log.info("Account creation processed successfully for {}", account.getAccountId());
    }

    private void ensureAccountId(CreateAccountEvent event) {
        if (event.getAccountId() == null || event.getAccountId().isEmpty()) {
            String generatedId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            event.setAccountId(generatedId);
            log.info("Generated account ID: {} for event", generatedId);
        }
    }

    private void publishSuccessNotificationForExistingAccount(String accountId, String rawMessage, CreateAccountEvent event, RequestType requestType) {
        Account existingAccount = accountMapper.findAccountById(accountId);
        List<Balance> existingBalances = balanceMapper.findBalancesByAccountId(accountId);
        existingAccount.setBalances(existingBalances);
        notificationService.publishSuccessNotification(
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.ACCOUNTS_PROCESSED_ROUTING_KEY.getValue(),
                requestType.getCode(),
                "SUCCESS",
                event.getRequestId(),
                existingAccount,
                event.getIdempotencyKey(),
                null
        );
    }

    private Account createAndSaveAccount(CreateAccountEvent event) {
        Account account = new Account();
        account.setAccountId(event.getAccountId());
        account.setCustomerId(event.getCustomerId());
        account.setCountry(event.getCountry());
        account.setIdempotencyKey(event.getIdempotencyKey());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountMapper.insertAccount(account);
        log.info("Created new account with ID: {}", account.getAccountId());
        return account;
    }

    private List<Balance> createAndSaveBalances(String accountId, List<Balance> balances) {
        for (Balance balance : balances) {
            balance.setAccountId(accountId);
            balance.setBalanceId(UUID.randomUUID().toString());
            balance.setVersionNumber(1);
            balance.setCreatedAt(LocalDateTime.now());
            balance.setUpdatedAt(LocalDateTime.now());
            balanceMapper.insertBalance(balance);
            log.info("Created balance for account {} with currency {}", accountId, balance.getCurrency().toString());
        }
        return balances;
    }

    private void recordProcessedMessage(String messageId, String accountId, String idempotencyKey) {
        ProcessedMessage processedMessage = new ProcessedMessage();
        processedMessage.setMessageId(messageId);
        processedMessage.setMessageType("CREATE_ACCOUNT");
        processedMessage.setIdempotencyKey(idempotencyKey);
        processedMessage.setProcessedAt(LocalDateTime.now());
        processedMessage.setResultData("{\"status\":\"SUCCESS\",\"accountId\":\"" + accountId + "\"}");
        
        processedMessageMapper.insertProcessedMessage(processedMessage);
        log.info("Recorded processed message for account {} with idempotency key {}", accountId, idempotencyKey);
    }
}