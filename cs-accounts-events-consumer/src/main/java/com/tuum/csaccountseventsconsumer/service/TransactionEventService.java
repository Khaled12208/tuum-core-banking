package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.common.domain.entities.ProcessedMessage;
import com.tuum.common.dto.mq.CreateTransactionEvent;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.types.RabbitMQConfig;
import com.tuum.common.exception.ProcessingException;
import com.tuum.common.types.ErrorCode;
import com.tuum.common.types.TransactionDirection;
import com.tuum.common.types.TransactionStatus;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import com.tuum.csaccountseventsconsumer.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventService {

    private final TransactionMapper transactionMapper;
    private final BalanceMapper balanceMapper;
    private final ProcessedMessageMapper processedMessageMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Transactional
    public void processTransactionCreatedEvent(MQMessageData messageData) {
        try {
            CreateTransactionEvent event = objectMapper.readValue(messageData.getMessageBody(), CreateTransactionEvent.class);
            

            
            switch (messageData.getRequestType()) {
                case CREATE:
                    handleCreate(event, messageData);
                    break;
                default:
                    throw new ProcessingException(
                            "Non supported process",
                            RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                            RabbitMQConfig.TRANSACTIONS_ERRORS_ROUTING_KEY.getValue(),
                            "TRANSACTION_PROCESSING_ERROR",
                            messageData.getRequestId(),
                            null
                    );
            }
        } catch (Exception e) {
            log.error("Error processing transaction: {}, idempotencyKey: {} , requestId: {}", e.getMessage(), messageData.getIdempotencyKey(), messageData.getRequestId());
            notificationService.publishErrorResponse(RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(), RabbitMQConfig.TRANSACTIONS_ERROR_ROUTING_KEY.getValue(), messageData, ErrorCode.TRANSACTION_CREATION_FAILED, e.getMessage());
        }
    }

    private void handleCreate(CreateTransactionEvent event, MQMessageData messageData) {
        String messageId = Optional.ofNullable(event.getIdempotencyKey()).orElse(event.getTransactionId());

        // Check database as source of truth
        if (processedMessageMapper.existsProcessedMessage(messageId)) {
            log.info("Transaction already processed in database: {}", messageId);
            publishSuccessNotificationForExistingTransaction(event.getAccountId(), messageData);
            return;
        }



        if (transactionMapper.existsTransactionByIdempotencyKey(event.getIdempotencyKey())) {
            log.warn("Transaction already exists with idempotency key: {}", event.getIdempotencyKey());
            throw businessException(event, "TRANSACTION_ALREADY_EXISTS", "Transaction already exists");
        }

        Balance balance = balanceMapper.findBalanceByAccountIdAndCurrency(event.getAccountId(), event.getCurrency());

        if (balance == null) {
            throw businessException(event, "BALANCE_NOT_FOUND",
                    "No " + event.getCurrency() + " balance found for account " + event.getAccountId());
        }

        // Set the balanceId in the event
        event.setBalanceId(balance.getBalanceId());

        BigDecimal newBalance = calculateNewBalance(balance, event);
        updateBalance(balance, newBalance);

        Transaction transaction = createAndInsertTransaction(event, newBalance);
        
        // Record the processed message as source of truth
        recordProcessedMessage(messageId, event.getTransactionId(), event.getIdempotencyKey());

        notificationService.publishSuccessNotification(
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.TRANSACTIONS_PROCESSED_ROUTING_KEY.getValue(),
                messageData.getRequestType().getCode(),
                "SUCCESS",
                messageData.getRequestId(),
                transaction,
                messageData.getIdempotencyKey(),
                null
        );

        log.info("Transaction processed successfully: {}", event.getTransactionId());
    }

    private BigDecimal calculateNewBalance(Balance balance, CreateTransactionEvent event) {
        if (TransactionDirection.IN == event.getDirection()) {
            return balance.getAvailableAmount().add(event.getAmount());
        } else if (TransactionDirection.OUT == event.getDirection()) {
            if (balance.getAvailableAmount().compareTo(event.getAmount()) < 0) {
                throw businessException(event, "INSUFFICIENT_FUNDS",
                        "Available: " + balance.getAvailableAmount() + ", Required: " + event.getAmount());
            }
            return balance.getAvailableAmount().subtract(event.getAmount());
        } else {
            throw businessException(event, "INVALID_DIRECTION", "Must be IN or OUT");
        }
    }

    private void updateBalance(Balance balance, BigDecimal newBalance) {
        int oldVersionNumber = balance.getVersionNumber();
        balance.setAvailableAmount(newBalance);
        balance.setVersionNumber(balance.getVersionNumber() + 1);
        balance.setUpdatedAt(LocalDateTime.now());
        
        int updatedRows = balanceMapper.updateBalance(balance, oldVersionNumber);
        if (updatedRows == 0) {
            throw new ProcessingException(
                    "Balance was modified by another transaction. Please retry.",
                    RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                    RabbitMQConfig.TRANSACTIONS_ERROR_ROUTING_KEY.getValue(),
                    "CONCURRENT_MODIFICATION",
                    balance.getAccountId(),
                    null
            );
        }
    }

    private Transaction createAndInsertTransaction(CreateTransactionEvent event, BigDecimal newBalance) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(event.getTransactionId());
        transaction.setAccountId(event.getAccountId());
        transaction.setBalanceId(event.getBalanceId());
        transaction.setAmount(event.getAmount());
        transaction.setCurrency(event.getCurrency());
        transaction.setDirection(event.getDirection());
        transaction.setDescription(event.getDescription());
        transaction.setBalanceAfterTransaction(newBalance);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setIdempotencyKey(event.getIdempotencyKey());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.insertTransaction(transaction);
        return transaction;
    }

    private void recordProcessedMessage(String messageId, String transactionId, String idempotencyKey) {
        ProcessedMessage processedMessage = new ProcessedMessage();
        processedMessage.setMessageId(messageId);
        processedMessage.setMessageType("CREATE_TRANSACTION");
        processedMessage.setIdempotencyKey(idempotencyKey);
        processedMessage.setProcessedAt(LocalDateTime.now());
        processedMessage.setResultData("{\"status\":\"SUCCESS\",\"transactionId\":\"" + transactionId + "\"}");

        processedMessageMapper.insertProcessedMessage(processedMessage);
        log.info("Recorded processed message with ID: {}", messageId);
    }

    private void publishSuccessNotificationForExistingTransaction(String transactionID, MQMessageData messageData) {
        Transaction existingTrans = transactionMapper.findTransactionById(transactionID);
        notificationService.publishSuccessNotification(
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.TRANSACTIONS_PROCESSED_ROUTING_KEY.getValue(),
                messageData.getRequestType().getCode(),
                "SUCCESS",
                messageData.getRequestId(),
                existingTrans,
                messageData.getIdempotencyKey(),
                null
        );
    }

    private ProcessingException businessException(CreateTransactionEvent event, String errorCode, String errorMessage) {
        return new ProcessingException(
                errorMessage,
                RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(),
                RabbitMQConfig.TRANSACTIONS_ERROR_ROUTING_KEY.getValue(),
                errorCode,
                event.getAccountId(),
                null
        );
    }
}
