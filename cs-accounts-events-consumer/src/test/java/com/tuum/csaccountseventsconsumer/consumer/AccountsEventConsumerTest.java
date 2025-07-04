package com.tuum.csaccountseventsconsumer.consumer;

import com.tuum.csaccountseventsconsumer.mapper.AccountMapper;
import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
import com.tuum.csaccountseventsconsumer.model.Account;
import com.tuum.csaccountseventsconsumer.model.Balance;
import com.tuum.csaccountseventsconsumer.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class AccountsEventConsumerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private BalanceMapper balanceMapper;
    @Mock
    private com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper processedMessageMapper;

    private ObjectMapper objectMapper;

    private AccountsEventConsumer accountsEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        accountsEventConsumer = new AccountsEventConsumer(
            notificationService, objectMapper, accountMapper, balanceMapper, processedMessageMapper
        );
    }

    @Test
    void testHandleAccountCreatedEvent_createsAccountAndBalances() {
        // Arrange
        String message = "{\"customerId\":\"CUST1\",\"country\":\"EE\",\"currencies\":[\"EUR\",\"USD\"]}";

        // Act
        accountsEventConsumer.handleAccountCreatedEvent(message);

        // Assert
        verify(accountMapper, times(1)).insertAccount(any(Account.class));
        verify(balanceMapper, atLeastOnce()).insertBalance(any(Balance.class));
    }
} 