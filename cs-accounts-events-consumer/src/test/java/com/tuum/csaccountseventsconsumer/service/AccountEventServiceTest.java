//package com.tuum.csaccountseventsconsumer.service;
//
//import com.tuum.csaccountseventsconsumer.mapper.AccountMapper;
//import com.tuum.csaccountseventsconsumer.mapper.BalanceMapper;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//
//class AccountEventServiceTest {
//
//    @Mock
//    private AccountMapper accountMapper;
//    @Mock
//    private BalanceMapper balanceMapper;
//    @Mock
//    private com.tuum.csaccountseventsconsumer.mapper.ProcessedMessageMapper processedMessageMapper;
//    @Mock
//    private NotificationService notificationService;
//
//    private ObjectMapper objectMapper;
//
//    private AccountEventService accountEventService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        objectMapper = new ObjectMapper();
//        accountEventService = new AccountEventService(
//            accountMapper, balanceMapper, processedMessageMapper, objectMapper, notificationService
//        );
//    }
//
//    @Test
//    void testProcessAccountCreatedEvent_createsAccountAndBalances() throws Exception {
//        // Arrange
////        CreateAccountEvent event = new CreateAccountEvent();
////        event.setCustomerId("CUST1");
////        event.setCountry("EE");
////        event.setBalances(List.of("EUR", "USD"));
////        String json = objectMapper.writeValueAsString(event);
////
////        // Act
////        accountEventService.processAccountCreatedEvent(json);
////
////        // Assert
////        verify(accountMapper, times(1)).insertAccount(any(Account.class));
////        verify(balanceMapper, times(2)).insertBalance(any(Balance.class));
//    }
//}