package com.tuum.fsaccountsservice.service;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.types.RabbitMQConfig;
import com.tuum.common.types.RequestType;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;
import com.tuum.common.dto.mq.CreateTransactionEvent;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.exception.InsufficientFundsException;
import com.tuum.common.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.mapper.BalanceMapper;
import com.tuum.fsaccountsservice.mapper.TransactionMapper;

import com.tuum.common.util.TraceIdGenerator;
import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.common.types.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.tuum.fsaccountsservice.dto.resonse.BalanceResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final EventPublisherService eventPublisherService;
    private final TraceIdGenerator traceIdGenerator;
    private final IdempotencyService idempotencyService;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, String idempotencyKey) {
        String requestId = traceIdGenerator.generateTraceId();
        log.info("Creating transaction with requestId: {} and idempotency key: {}", requestId, idempotencyKey);
        

        
        // Fast in-memory idempotency check
        if (idempotencyService.isProcessed(idempotencyKey)) {
            log.info("Transaction already processed in memory, skipping: {}", idempotencyKey);
            // Return existing transaction from database if available
            Transaction existingTransaction = transactionMapper.findTransactionByIdempotencyKey(idempotencyKey);
            if (existingTransaction != null) {
                return createResponseFromTransaction(existingTransaction);
            }
            throw new BusinessException("Transaction already processed but not found in database");
        }
        
        // Mark as processed in memory immediately to prevent duplicate processing
        idempotencyService.markAsProcessed(idempotencyKey);
        
        try {
            Account account = accountMapper.findAccountById(request.getAccountId());
            if (account == null) {
                throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
            }
            CreateTransactionEvent event = new CreateTransactionEvent();
            event.setTransactionId(java.util.UUID.randomUUID().toString());
            event.setAccountId(request.getAccountId());
            event.setAmount(request.getAmount());
            event.setCurrency(request.getCurrency());
            event.setDirection(request.getDirection());
            event.setDescription(request.getDescription());
            event.setIdempotencyKey(idempotencyKey);
            event.setCreatedAt(LocalDateTime.now());
            log.info("Event: {}", event);
            log.info("Exchange: {}", RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue());
            log.info("Routing key: {}", RabbitMQConfig.TRANSACTIONS_CREATED_ROUTING_KEY.getValue());

            return eventPublisherService.publishEventAndWaitForResponse(
                event, 
                RabbitMQConfig.TRANSACTIONS_CREATED_ROUTING_KEY.getValue(),
                idempotencyKey,
                    requestId,
                30,
                    RequestType.CREATE
            );
        } catch (BusinessException e) {
            log.error("Business error creating transaction: {}", idempotencyKey, e);
            throw e; // Re-throw business exceptions as-is
        } catch (Exception e) {
            log.error("Error creating transaction: {}", idempotencyKey, e);
            throw new BusinessException("Failed to create transaction: " + e.getMessage());
        } finally {
            traceIdGenerator.clear();
        }
    }

    public void completeTransaction(Object event, MQMessageData messageData) {
        String idempotencyKey = messageData.getIdempotencyKey();
        String status = messageData.getStatus();

        if ("SUCCESS".equalsIgnoreCase(status) && event instanceof CreateTransactionEvent transactionCreatedEvent) {
            TransactionResponse response = new TransactionResponse();
            response.setTransactionId(transactionCreatedEvent.getTransactionId());
            response.setAccountId(transactionCreatedEvent.getAccountId());
            response.setAmount(transactionCreatedEvent.getAmount());
            response.setCurrency(transactionCreatedEvent.getCurrency());
            response.setDirection(transactionCreatedEvent.getDirection());
            response.setDescription(transactionCreatedEvent.getDescription());
            response.setBalanceAfterTransaction(transactionCreatedEvent.getBalanceAfterTransaction());
            eventPublisherService.completeRequest(idempotencyKey, response);
        } else
            eventPublisherService.completeRequest(idempotencyKey, event);
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(String transactionId) {
        Transaction transaction = transactionMapper.findTransactionById(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found with id: " + transactionId);
        }
        return transaction;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAccountTransactions(String accountId) {
        return transactionMapper.findTransactionsByAccountId(accountId);
    }

    private TransactionResponse createResponseFromTransaction(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setAccountId(transaction.getAccountId());
        response.setBalanceId(transaction.getBalanceId());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setDirection(transaction.getDirection());
        response.setDescription(transaction.getDescription());
        response.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());
        response.setStatus(transaction.getStatus().name());
        response.setIdempotencyKey(transaction.getIdempotencyKey());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        
        // Fetch and include balance information
        if (transaction.getBalanceId() != null) {
            var balance = balanceMapper.findBalanceById(transaction.getBalanceId());
            if (balance != null) {
                BalanceResponse balanceResponse = new BalanceResponse();
                balanceResponse.setBalanceId(balance.getBalanceId());
                balanceResponse.setAccountId(balance.getAccountId());
                balanceResponse.setCurrency(balance.getCurrency());
                balanceResponse.setAvailableAmount(balance.getAvailableAmount());
                balanceResponse.setCreatedAt(balance.getCreatedAt());
                balanceResponse.setUpdatedAt(balance.getUpdatedAt());
                response.setBalance(balanceResponse);
            }
        }
        
        return response;
    }

} 