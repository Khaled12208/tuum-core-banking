//package com.tuum.csaccountseventsconsumer.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.tuum.csaccountseventsconsumer.service.AccountEventService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//class AccountsEventConsumerTest {
//
//    @Mock
//    private AccountEventService accountEventService;
//
//    private ObjectMapper objectMapper;
//
//    private AccountsEventConsumer accountsEventConsumer;
//
////    @BeforeEach
////    void setUp() {
////        MockitoAnnotations.openMocks(this);
////        objectMapper = new ObjectMapper();
////        accountsEventConsumer = new AccountsEventConsumer(
////            accountEventService, objectMapper
////        );
////    }
//
////    @Test
////    void testHandleAccountCreatedEvent_callsService() {
////        // Arrange
////        String message = "{\"customerId\":\"CUST1\",\"country\":\"EE\",\"currencies\":[\"EUR\",\"USD\"]}";
////
////        // Act
////        accountsEventConsumer.handleAccountCreatedEvent(message);
////
////        // Assert
////        verify(accountEventService, times(1)).processAccountEvent(message);
////    }
//}