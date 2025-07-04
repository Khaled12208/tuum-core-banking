package com.tuum.fsaccountsservice.service;

import com.tuum.fsaccountsservice.mapper.TransactionMapper;
import com.tuum.fsaccountsservice.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTransactionById() {
        // Arrange
        String transactionId = "TXN123";
        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(transactionId);
        when(transactionMapper.findTransactionById(transactionId)).thenReturn(mockTransaction);

        // Act
        Transaction result = transactionService.getTransactionById(transactionId);

        // Assert
        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        verify(transactionMapper, times(1)).findTransactionById(transactionId);
    }
} 