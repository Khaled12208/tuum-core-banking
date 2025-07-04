package com.tuum.csaccountseventsconsumer.consumer;

import com.tuum.csaccountseventsconsumer.service.TransactionEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class TransactionEventConsumerTest {

    @Mock
    private TransactionEventService transactionEventService;

    @InjectMocks
    private TransactionEventConsumer transactionEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleTransactionCreatedEvent_delegatesToService() {
        // Arrange
        String message = "{\"transactionId\":\"TXN1\",\"accountId\":\"ACC1\",\"amount\":100.50,\"currency\":\"EUR\",\"direction\":\"IN\",\"description\":\"Deposit\",\"idempotencyKey\":\"IDEMP1\"}";

        // Act
        transactionEventConsumer.handleTransactionCreatedEvent(message);

        // Assert
        verify(transactionEventService, times(1)).processTransactionCreatedEvent(message);
    }
} 