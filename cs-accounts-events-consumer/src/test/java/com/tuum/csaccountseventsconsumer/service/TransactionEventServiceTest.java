//package com.tuum.csaccountseventsconsumer.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.tuum.common.domain.entities.Balance;
//import com.tuum.common.domain.entities.Transaction;
//import com.tuum.common.dto.mq.CreateTransactionEvent;
//import com.tuum.common.types.Currency;
//import com.tuum.common.types.TransactionDirection;
//import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
//import com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper;
//import com.tuum.csaccountseventsconsumer.mapper.TransactionMapper;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.amqp.core.AmqpTemplate;
//
//
//class TransactionEventServiceTest {
//
//    @Mock
//    private TransactionMapper transactionMapper;
//    @Mock
//    private BalanceMapper balanceMapper;
//    @Mock
//    private ProcessedMessageMapper processedMessageMapper;
//    @Mock
//    private ObjectMapper objectMapper;
//    @Mock
//    private NotificationService notificationService;
//    @Mock
//    private AmqpTemplate amqpTemplate;
//
//    @InjectMocks
//    private TransactionEventService transactionEventService;
//
////    private MockedStatic<Helper> helperMock;
//
//    @BeforeEach
//    void setUp() {
////        MockitoAnnotations.openMocks(this);
////        helperMock = mockStatic(Helper.class);
////        helperMock.when(() -> Helper.unwrapIfDoubleEncoded(anyString(), any(ObjectMapper.class)))
////                  .thenAnswer(invocation -> invocation.getArgument(0));
//    }
//
////    @org.junit.jupiter.api.AfterEach
////    void tearDown() {
////        if (helperMock != null) {
////            helperMock.close();
////        }
////    }
//
////    @Test
////    void testProcessTransactionCreatedEvent_updatesBalanceAndCreatesTransaction() throws Exception {
////        // Arrange
////        CreateTransactionEvent event = new CreateTransactionEvent();
////        event.setTransactionId("TXN1");
////        event.setAccountId("ACC1");
////        event.setAmount(new BigDecimal("100.00"));
////        event.setCurrency(Currency.EUR);
////        event.setDirection(TransactionDirection.IN);
////        event.setDescription("Deposit");
////        event.setIdempotencyKey("IDEMP1");
////        event.setRequestId("REQ1");
////
////        String json = "{\"transactionId\":\"TXN1\",\"accountId\":\"ACC1\",\"amount\":100.00,\"currency\":\"EUR\",\"direction\":\"IN\",\"description\":\"Deposit\",\"idempotencyKey\":\"IDEMP1\",\"requestId\":\"REQ1\"}";
////
////        // Mock the Helper.unwrapIfDoubleEncoded method
////        when(objectMapper.readTree(json)).thenReturn(new ObjectMapper().readTree(json));
////        when(objectMapper.treeToValue(any(), eq(CreateTransactionEvent.class))).thenReturn(event);
////
////        when(processedMessageMapper.existsProcessedMessage("IDEMP1")).thenReturn(false);
////        when(transactionMapper.existsTransactionByIdempotencyKey("IDEMP1")).thenReturn(false);
////
////        Balance balance = new Balance();
////        balance.setAccountId("ACC1");
////        balance.setCurrency(Currency.EUR);
////        balance.setAvailableAmount(new BigDecimal("0.00"));
////        balance.setVersionNumber(1);
////        when(balanceMapper.findBalanceByAccountIdAndCurrency("ACC1", "EUR")).thenReturn(balance);
////        when(balanceMapper.updateBalance(any(Balance.class), anyInt())).thenReturn(1);
////        doNothing().when(transactionMapper).insertTransaction(any(Transaction.class));
////
////        // Act
////        transactionEventService.processTransactionCreatedEvent(json);
////
////        // Assert
////        // Verify the service methods were called
////        verify(balanceMapper, times(1)).findBalanceByAccountIdAndCurrency("ACC1", "EUR");
////        verify(balanceMapper, times(1)).updateBalance(any(Balance.class), anyInt());
////        verify(transactionMapper, times(1)).insertTransaction(any(Transaction.class));
////    }
//}