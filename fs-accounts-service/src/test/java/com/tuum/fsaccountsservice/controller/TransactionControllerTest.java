package com.tuum.fsaccountsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.exception.InsufficientFundsException;
import com.tuum.common.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;
import com.tuum.fsaccountsservice.service.TransactionService;
import com.tuum.fsaccountsservice.utils.TestConstants;
import com.tuum.fsaccountsservice.utils.TestDataBuilder;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @CsvSource({
        "ACC123, 100.00, EUR, IN, Payment for invoice #1234",
        "ACC456, 200.00, SEK, OUT, Withdrawal",
        "ACC789, 300.00, GBP, IN, Deposit",
        "ACC101, 150.50, USD, IN, Salary payment",
        "ACC202, 75.25, EUR, OUT, Shopping"
    })
    void testCreateTransaction_Success(String accountId, BigDecimal amount, Currency currency, TransactionDirection direction, String description) throws Exception {
        CreateTransactionRequest request = TestDataBuilder.createTransactionRequest()
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .direction(direction)
                .description(description)
                .build();

        TransactionResponse mockResponse = TestDataBuilder.transactionResponse()
                .transactionId(TestConstants.DEFAULT_TRANSACTION_ID)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .direction(direction)
                .description(description)
                .build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestConstants.IDEMPOTENCY_KEY_HEADER, TestConstants.TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(TestConstants.DEFAULT_TRANSACTION_ID))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(amount.doubleValue()))
                .andExpect(jsonPath("$.currency").value(currency.name()))
                .andExpect(jsonPath("$.direction").value(direction.name()))
                .andExpect(jsonPath("$.description").value(description));

        verify(transactionService, times(1)).createTransaction(any(CreateTransactionRequest.class), eq(TestConstants.TEST_IDEMPOTENCY_KEY));
    }

    @Test
    void testCreateTransaction_MissingIdempotencyKey_ThrowsException() throws Exception {
        CreateTransactionRequest request = TestDataBuilder.createTransactionRequest().build();

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TXN123", "TXN456", "TXN789", "TXN101", "TXN202"})
    void testGetTransaction_Success(String transactionId) throws Exception {
        // Arrange
        Transaction mockTransaction = TestDataBuilder.transaction()
                .transactionId(transactionId)
                .accountId(TestConstants.DEFAULT_ACCOUNT_ID)
                .currency(TestConstants.DEFAULT_CURRENCY)
                .amount(TestConstants.DEFAULT_TRANSACTION_AMOUNT)
                .direction(TestConstants.DEFAULT_DIRECTION)
                .build();

        when(transactionService.getTransaction(transactionId)).thenReturn(mockTransaction);

        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.accountId").value(TestConstants.DEFAULT_ACCOUNT_ID))
                .andExpect(jsonPath("$.amount").value(TestConstants.DEFAULT_TRANSACTION_AMOUNT))
                .andExpect(jsonPath("$.currency").value(TestConstants.DEFAULT_CURRENCY.name()))
                .andExpect(jsonPath("$.direction").value(TestConstants.DEFAULT_DIRECTION.name()));

        verify(transactionService, times(1)).getTransaction(transactionId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TXN123", "TXN456", "TXN789", "TXN101", "TXN202"})
    void testGetTransaction_NotFound_ThrowsException(String transactionId) throws Exception {
        when(transactionService.getTransaction(transactionId))
                .thenThrow(new ResourceNotFoundException(TestConstants.TRANSACTION_NOT_FOUND_MESSAGE + transactionId));

        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isNotFound());

        verify(transactionService, times(1)).getTransaction(transactionId);
    }

    @ParameterizedTest
    @CsvSource({
        "ACC123, 2",
        "ACC456, 1",
        "ACC789, 3",
        "ACC101, 2",
        "ACC202, 1"
    })
    void testGetAccountTransactions_Success(String accountId, int expectedCount) throws Exception {
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

        if (expectedCount == 1) {
            mockTransactions = Arrays.asList(mockTransactions.get(0));
        } else if (expectedCount == 3) {
            mockTransactions = Arrays.asList(
                    mockTransactions.get(0),
                    mockTransactions.get(1),
                    TestDataBuilder.transaction()
                            .transactionId("TXN3")
                            .accountId(accountId)
                            .currency(Currency.GBP)
                            .amount(BigDecimal.valueOf(300.00))
                            .direction(TransactionDirection.IN)
                            .build()
            );
        }

        when(transactionService.getAccountTransactions(accountId)).thenReturn(mockTransactions);

        // Act & Assert
        mockMvc.perform(get("/transactions/account/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(expectedCount));

        verify(transactionService, times(1)).getAccountTransactions(accountId);
    }

    @Test
    void testCreateTransaction_ServiceThrowsInsufficientFundsException_ReturnsError() throws Exception {
        CreateTransactionRequest request = TestDataBuilder.createTransactionRequest().build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class), anyString()))
                .thenThrow(new InsufficientFundsException("Insufficient funds"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestConstants.IDEMPOTENCY_KEY_HEADER, TestConstants.TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_ServiceThrowsBusinessException_ReturnsError() throws Exception {
        CreateTransactionRequest request = TestDataBuilder.createTransactionRequest().build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class), anyString()))
                .thenThrow(new BusinessException("Transaction creation failed"));
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestConstants.IDEMPOTENCY_KEY_HEADER, TestConstants.TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAccountTransactions_ServiceThrowsResourceNotFoundException_ReturnsError() throws Exception {
        String accountId = "ACC123";
        when(transactionService.getAccountTransactions(accountId))
                .thenThrow(new ResourceNotFoundException("Account not found"));
        mockMvc.perform(get("/transactions/account/{accountId}", accountId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTransaction_ServiceThrowsResourceNotFoundException_ReturnsError() throws Exception {
        String transactionId = "TXN123";
        when(transactionService.getTransaction(transactionId))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));
        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isNotFound());
    }

    @ControllerAdvice
    static class TestExceptionHandler {
        
        @ExceptionHandler(BusinessException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ResponseBody
        public String handleBusinessException(BusinessException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
        
        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        @ResponseBody
        public String handleResourceNotFoundException(ResourceNotFoundException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
        
        @ExceptionHandler(InsufficientFundsException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ResponseBody
        public String handleInsufficientFundsException(InsufficientFundsException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}