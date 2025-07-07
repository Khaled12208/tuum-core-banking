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

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void testGetAccountTransactions_Success() {
        String accountId = "ACC123";
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
        verify(transactionMapper, times(1)).findTransactionsByAccountId(accountId);
    }
}