package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.domain.entities.ProcessedMessage;
import com.tuum.common.dto.mq.CreateAccountEvent;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.types.RabbitMQConfig;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.types.ErrorCode;
import com.tuum.common.util.IdGenerator;
import com.tuum.csaccountseventsconsumer.mapper.AccountMapper;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final ProcessedMessageMapper processedMessageMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Transactional
    public void processAccountCreatedEvent(MQMessageData messageData) {
        try {
            CreateAccountEvent event = objectMapper.readValue(messageData.getMessageBody(), CreateAccountEvent.class);
            
            switch (messageData.getRequestType()) {
                case CREATE:
                    handleCreate(event, messageData);
                    break;
                default:
                    throw new BusinessException(
                            "Non supported process",
                            "ACCOUNT_PROCESSING_ERROR"
                    );
            }
        } catch (Exception e) {
            log.error("Error processing account: {}, idempotencyKey: {} , requestId: {}", e.getMessage(), messageData.getIdempotencyKey(), messageData.getRequestId());
            notificationService.publishErrorResponse(RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(), RabbitMQConfig.ACCOUNTS_ERROR_ROUTING_KEY.getValue(), messageData, ErrorCode.ACCOUNT_CREATION_FAILED, e.getMessage());
        }
    }

    private void handleCreate(CreateAccountEvent event, MQMessageData messageData) {
        String messageId = Optional.ofNullable(event.getIdempotencyKey()).orElse(event.getAccountId());

        if (processedMessageMapper.existsProcessedMessage(messageId)) {
            log.info("Account already processed in database: {}", messageId);
            publishSuccessNotificationForExistingAccount(event.getAccountId(), messageData);
            return;
        }

        if (accountMapper.existsAccountByIdempotencyKey(event.getIdempotencyKey())) {
            log.warn("Account already exists with idempotency key: {}", event.getIdempotencyKey());
            throw new BusinessException(
                    "Account already exists with idempotency key: " + event.getIdempotencyKey(),
                    "ACCOUNT_ALREADY_EXISTS"
            );
        }

        Account account = createAndInsertAccount(event);
        List<Balance> balances = createAndInsertBalances(event, account.getAccountId());
        account.setBalances(balances);
        recordProcessedMessage(messageId, account.getAccountId(), event.getIdempotencyKey());

        Map<String, Object> extraHeaders = new HashMap<>();
        extraHeaders.put("balances", balances);

        notificationService.publishSuccessNotification(
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.ACCOUNTS_PROCESSED_ROUTING_KEY.getValue(),
                messageData.getRequestType().getCode(),
                "SUCCESS",
                messageData.getRequestId(),
                account,
                messageData.getIdempotencyKey(),
                extraHeaders
        );

        log.info("Account processed successfully: {}", account.getAccountId());
    }

    private Account createAndInsertAccount(CreateAccountEvent event) {
        Account account = new Account();
        // Use thread-safe ID generator for account ID
        String accountId = event.getAccountId();
        if (accountId == null || accountId.isEmpty()) {
            // Generate new account ID using thread-safe generator
            accountId = IdGenerator.generateAccountId();
            log.info("Generated new account ID: {}", accountId);
        }
        account.setAccountId(accountId);
        account.setCustomerId(event.getCustomerId());
        account.setCountry(event.getCountry());
        account.setIdempotencyKey(event.getIdempotencyKey());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        accountMapper.insertAccount(account);
        return account;
    }

    private List<Balance> createAndInsertBalances(CreateAccountEvent event, String accountId) {
        return event.getBalances().stream()
                .map(balance -> {
                    balance.setAccountId(accountId);
                    // Use thread-safe ID generator for balance ID
                    balance.setBalanceId(IdGenerator.generateBalanceId());
                    balance.setAvailableAmount(BigDecimal.ZERO);
                    balance.setVersionNumber(1);
                    balance.setCreatedAt(LocalDateTime.now());
                    balance.setUpdatedAt(LocalDateTime.now());
                    balanceMapper.insertBalance(balance);
                    return balance;
                })
                .toList();
    }

    private void recordProcessedMessage(String messageId, String accountId, String idempotencyKey) {
        ProcessedMessage processedMessage = new ProcessedMessage();
        processedMessage.setMessageId(messageId);
        processedMessage.setMessageType("CREATE_ACCOUNT");
        processedMessage.setIdempotencyKey(idempotencyKey);
        processedMessage.setProcessedAt(LocalDateTime.now());
        processedMessage.setResultData("{\"status\":\"SUCCESS\",\"accountId\":\"" + accountId + "\"}");

        processedMessageMapper.insertProcessedMessage(processedMessage);
        log.info("Recorded processed message with ID: {}", messageId);
    }

    private void publishSuccessNotificationForExistingAccount(String accountId, MQMessageData messageData) {
        Account existingAccount = accountMapper.findAccountById(accountId);
        List<Balance> existingBalances = balanceMapper.findBalancesByAccountId(accountId);
        
        // Create extra headers for balances
        Map<String, Object> extraHeaders = new HashMap<>();
        extraHeaders.put("balances", existingBalances);
        
        notificationService.publishSuccessNotification(
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.ACCOUNTS_PROCESSED_ROUTING_KEY.getValue(),
                messageData.getRequestType().getCode(),
                "SUCCESS",
                messageData.getRequestId(),
                existingAccount,
                messageData.getIdempotencyKey(),
                extraHeaders
        );
    }
}