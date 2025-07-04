package com.tuum.fsaccountsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.fsaccountsservice.dto.AccountProcessedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TransactionService transactionService;
    @Mock
    private AccountService accountService;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleAccountNotification_sendsWebSocketNotification() throws Exception {
        // Arrange
        String json = "{\"accountId\":\"ACC1\",\"customerId\":\"CUST1\",\"country\":\"EE\",\"currencies\":[\"EUR\"],\"balances\":[],\"status\":\"SUCCESS\",\"processedAt\":\"2023-01-01T00:00:00\",\"requestId\":\"REQ1\"}";
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(json.getBytes());
        AccountProcessedEvent event = new AccountProcessedEvent();
        event.setAccountId("ACC1");
        event.setCustomerId("CUST1");
        event.setCountry("EE");
        event.setStatus("SUCCESS");
        event.setRequestId("REQ1");
        when(objectMapper.readValue(json, AccountProcessedEvent.class)).thenReturn(event);

        // Act
        webSocketNotificationService.handleAccountNotification(message);

        // Assert
        verify(accountService, times(1)).completeAccount("REQ1", event);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/accounts"), (Object) any());
    }
} 