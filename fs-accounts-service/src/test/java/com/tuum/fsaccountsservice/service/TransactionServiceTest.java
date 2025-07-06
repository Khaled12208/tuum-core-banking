package com.tuum.fsaccountsservice.service;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import com.tuum.common.types.TransactionStatus;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.mapper.BalanceMapper;
import com.tuum.fsaccountsservice.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private TransactionService transactionService;

    @ParameterizedTest
    @CsvSource({
        "TXN123, ACC123, EUR, 100.00, IN",
        "TXN456, ACC456, SEK, 200.00, OUT",
        "TXN789, ACC789, GBP, 300.00, IN"
    })
    void getTransaction_Success(String transactionId, String accountId, Currency currency, BigDecimal amount, TransactionDirection direction) {
        // Given
        Transaction mockTransaction = createMockTransaction(transactionId, accountId, currency, amount, direction);
        when(transactionMapper.findTransactionById(transactionId)).thenReturn(mockTransaction);

        // When
        Transaction result = transactionService.getTransaction(transactionId);

        // Then
        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(currency, result.getCurrency());
        assertEquals(amount, result.getAmount());
        assertEquals(direction, result.getDirection());

        verify(transactionMapper, times(1)).findTransactionById(transactionId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TXN123", "TXN456", "TXN789"})
    void getTransaction_NotFound_ThrowsException(String transactionId) {
        // Given
        when(transactionMapper.findTransactionById(transactionId)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transactionService.getTransaction(transactionId);
        });

        verify(transactionMapper, times(1)).findTransactionById(transactionId);
    }

    @ParameterizedTest
    @CsvSource({
        "ACC123, 2",
        "ACC456, 2",
        "ACC789, 2"
    })
    void getAccountTransactions_Success(String accountId, int expectedCount) {
        // Given
        List<Transaction> mockTransactions = Arrays.asList(
            createMockTransaction("TXN1", accountId, Currency.EUR, new BigDecimal("100.00"), TransactionDirection.IN),
            createMockTransaction("TXN2", accountId, Currency.SEK, new BigDecimal("200.00"), TransactionDirection.OUT)
        );
        when(transactionMapper.findTransactionsByAccountId(accountId)).thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionService.getAccountTransactions(accountId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCount, result.size());
        result.forEach(transaction -> assertEquals(accountId, transaction.getAccountId()));

        verify(transactionMapper, times(1)).findTransactionsByAccountId(accountId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACC123", "ACC456", "ACC789"})
    void getAccountTransactions_EmptyList(String accountId) {
        // Given
        when(transactionMapper.findTransactionsByAccountId(accountId)).thenReturn(Arrays.asList());

        // When
        List<Transaction> result = transactionService.getAccountTransactions(accountId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionMapper, times(1)).findTransactionsByAccountId(accountId);
    }

    // Helper methods
    private Transaction createMockTransaction(String transactionId, String accountId, Currency currency, BigDecimal amount, TransactionDirection direction) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setBalanceId(UUID.randomUUID().toString());
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDirection(direction);
        transaction.setDescription("Test transaction");
        transaction.setBalanceAfterTransaction(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setIdempotencyKey("test-key-" + transactionId);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        return transaction;
    }

    private Account createMockAccount(String accountId, String customerId, String country) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCustomerId(customerId);
        account.setCountry(country);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private Balance createMockBalance(String accountId, Currency currency, BigDecimal amount) {
        Balance balance = new Balance();
        balance.setBalanceId(UUID.randomUUID().toString());
        balance.setAccountId(accountId);
        balance.setCurrency(currency);
        balance.setAvailableAmount(amount);
        balance.setVersionNumber(1);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());
        return balance;
    }
}