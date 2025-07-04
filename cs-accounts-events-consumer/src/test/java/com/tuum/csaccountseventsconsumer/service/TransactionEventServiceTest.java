package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.csaccountseventsconsumer.dto.TransactionCreatedEvent;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
import com.tuum.csaccountseventsconsumer.mapper.TransactionMapper;
import com.tuum.csaccountseventsconsumer.model.Balance;
import com.tuum.csaccountseventsconsumer.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class TransactionEventServiceTest {

    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private BalanceMapper balanceMapper;
    @Mock
    private ProcessedMessageMapper processedMessageMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private TransactionEventService transactionEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessTransactionCreatedEvent_updatesBalanceAndCreatesTransaction() throws Exception {
        // Arrange
        TransactionCreatedEvent event = new TransactionCreatedEvent();
        event.setTransactionId("TXN1");
        event.setAccountId("ACC1");
        event.setAmount(new BigDecimal("100.00"));
        event.setCurrency("EUR");
        event.setDirection("IN");
        event.setDescription("Deposit");
        event.setIdempotencyKey("IDEMP1");
        String json = new ObjectMapper().writeValueAsString(event);
        when(objectMapper.readValue(json, TransactionCreatedEvent.class)).thenReturn(event);
        when(processedMessageMapper.existsProcessedMessage("IDEMP1")).thenReturn(false);
        when(transactionMapper.existsTransactionByIdempotencyKey("IDEMP1")).thenReturn(false);
        Balance balance = new Balance();
        balance.setAccountId("ACC1");
        balance.setCurrency("EUR");
        balance.setAvailableAmount(new BigDecimal("0.00"));
        balance.setVersionNumber(1);
        when(balanceMapper.findBalanceByAccountIdAndCurrency("ACC1", "EUR")).thenReturn(balance);

        // Act
        transactionEventService.processTransactionCreatedEvent(json);

        // Assert
        verify(balanceMapper, times(1)).updateBalance(any(Balance.class));
        verify(transactionMapper, times(1)).insertTransaction(any(Transaction.class));
    }
} 