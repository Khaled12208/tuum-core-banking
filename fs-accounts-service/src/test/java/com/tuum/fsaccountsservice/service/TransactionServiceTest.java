package com.tuum.fsaccountsservice.service;

import com.tuum.common.domain.entities.Transaction;
import com.tuum.fsaccountsservice.mapper.TransactionMapper;
import com.tuum.fsaccountsservice.utils.TestDataBuilder;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tuum.common.exception.BusinessException;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.common.domain.entities.Account;
import com.tuum.common.dto.mq.CreateTransactionEvent;
import com.tuum.common.types.RequestType;
import com.tuum.fsaccountsservice.service.IdempotencyService;
import com.tuum.fsaccountsservice.service.EventPublisherService;
import com.tuum.common.exception.InsufficientFundsException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void testGetAccountTransactions_Success() {
        String accountId = "ACC123";
        
        // Mock account existence check
        Account mockAccount = TestDataBuilder.account()
                .accountId(accountId)
                .customerId("CUST123")
                .country("EE")
                .build();
        when(accountMapper.findAccountById(accountId)).thenReturn(mockAccount);
        
        List<Transaction> mockTransactions = Arrays.asList(
                TestDataBuilder.transaction()
                        .transactionId("TXN1")
                        .accountId(accountId)
                        .currency(Currency.EUR)
                        .amount(BigDecimal.valueOf(100.00))
                        .direction(TransactionDirection.IN)
                        .build(),
                TestDataBuilder.transaction()
                        .transactionId("TXN2")
                        .accountId(accountId)
                        .currency(Currency.SEK)
                        .amount(BigDecimal.valueOf(200.00))
                        .direction(TransactionDirection.OUT)
                        .build()
        );
        when(transactionMapper.findTransactionsByAccountId(accountId)).thenReturn(mockTransactions);
        
        List<Transaction> result = transactionService.getAccountTransactions(accountId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(transaction -> assertEquals(accountId, transaction.getAccountId()));
        verify(accountMapper, times(1)).findAccountById(accountId);
        verify(transactionMapper, times(1)).findTransactionsByAccountId(accountId);
    }

    @Test
    void testCreateTransaction_ThrowsCleanBusinessException() throws Exception {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("test-account");
        request.setAmount(new BigDecimal("25.00"));
        request.setCurrency(Currency.EUR);
        request.setDirection(TransactionDirection.OUT);
        request.setDescription("Test transaction");
        
        String idempotencyKey = "test-key-123";
        
        when(idempotencyService.isProcessed(idempotencyKey)).thenReturn(false);
        when(accountMapper.findAccountById("test-account")).thenReturn(new Account());
        
        BusinessException expectedException = new BusinessException("Available: 2.00, Required: 25.00");
        when(eventPublisherService.publishEventAndWaitForResponse(
            any(CreateTransactionEvent.class),
            anyString(),
            eq(idempotencyKey),
            anyString(),
            eq(30),
            eq(RequestType.CREATE)
        )).thenThrow(expectedException);
        
        // When & Then
        BusinessException actualException = assertThrows(BusinessException.class, () -> {
            transactionService.createTransaction(request, idempotencyKey);
        });
        
        // Verify the exception message is clean and not double-wrapped
        assertEquals("Available: 2.00, Required: 25.00", actualException.getMessage());
        assertFalse(actualException.getMessage().contains("Failed to create transaction:"));
        assertFalse(actualException.getMessage().contains("Request failed:"));
        assertFalse(actualException.getMessage().contains("com.tuum.common.exception.BusinessException"));
    }

    @Test
    void testCreateTransaction_ThrowsInsufficientFundsException() throws Exception {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("test-account");
        request.setAmount(new BigDecimal("25.00"));
        request.setCurrency(Currency.EUR);
        request.setDirection(TransactionDirection.OUT);
        request.setDescription("Test transaction");
        
        String idempotencyKey = "test-key-123";
        
        when(idempotencyService.isProcessed(idempotencyKey)).thenReturn(false);
        when(accountMapper.findAccountById("test-account")).thenReturn(new Account());
        
        InsufficientFundsException expectedException = new InsufficientFundsException("Available: 0.00, Required: 25.00");
        when(eventPublisherService.publishEventAndWaitForResponse(
            any(CreateTransactionEvent.class),
            anyString(),
            eq(idempotencyKey),
            anyString(),
            eq(30),
            eq(RequestType.CREATE)
        )).thenThrow(expectedException);
        
        // When & Then
        InsufficientFundsException actualException = assertThrows(InsufficientFundsException.class, () -> {
            transactionService.createTransaction(request, idempotencyKey);
        });
        
        // Verify the exception message is clean and correct
        assertEquals("Available: 0.00, Required: 25.00", actualException.getMessage());
    }
}