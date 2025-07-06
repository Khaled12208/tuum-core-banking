package com.tuum.fsaccountsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;
import com.tuum.fsaccountsservice.service.TransactionService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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
                .build();
        objectMapper = new ObjectMapper();
    }

    // @ParameterizedTest
    // @CsvSource({
    //     "ACC123, 100.00, EUR, IN, Payment for invoice #1234",
    //     "ACC456, 200.00, SEK, OUT, Withdrawal",
    //     "ACC789, 300.00, GBP, IN, Deposit"
    // })
    // void testCreateTransaction_Success(String accountId, BigDecimal amount, Currency currency, TransactionDirection direction, String description) throws Exception {
    //     // Arrange
    //     CreateTransactionRequest request = new CreateTransactionRequest();
    //     request.setAccountId(accountId);
    //     request.setAmount(amount);
    //     request.setCurrency(currency);
    //     request.setDirection(direction);
    //     request.setDescription(description);

    //     TransactionResponse mockResponse = createMockTransactionResponse(accountId, amount, currency, direction, description);

    //     when(transactionService.createTransaction(any(CreateTransactionRequest.class), anyString()))
    //             .thenReturn(mockResponse);

    //     // Act & Assert
    //     mockMvc.perform(post("/transactions")
    //                     .contentType(MediaType.APPLICATION_JSON)
    //                     .header("Idempotency-Key", "test-txn-key-123")
    //                     .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.transactionId").value("TXN123"))
    //             .andExpect(jsonPath("$.accountId").value(accountId))
    //             .andExpect(jsonPath("$.amount").value(amount))
    //             .andExpect(jsonPath("$.currency").value(currency.name()))
    //             .andExpect(jsonPath("$.direction").value(direction.name()))
    //             .andExpect(jsonPath("$.description").value(description));

    //     verify(transactionService, times(1)).createTransaction(any(CreateTransactionRequest.class), eq("test-txn-key-123"));
    // }

    // @ParameterizedTest
    // @ValueSource(strings = {"TXN123", "TXN456", "TXN789"})
    // void testGetTransaction_Success(String transactionId) throws Exception {
    //     // Arrange
    //     Transaction mockTransaction = createMockTransaction(transactionId, "ACC123", Currency.EUR, new BigDecimal("100.00"), TransactionDirection.IN);
        
    //     when(transactionService.getTransaction(transactionId)).thenReturn(mockTransaction);

    //     // Act & Assert
    //     mockMvc.perform(get("/transactions/" + transactionId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.transactionId").value(transactionId))
    //             .andExpect(jsonPath("$.accountId").value("ACC123"))
    //             .andExpect(jsonPath("$.amount").value(100.00))
    //             .andExpect(jsonPath("$.currency").value("EUR"))
    //             .andExpect(jsonPath("$.direction").value("IN"));

    //     verify(transactionService, times(1)).getTransaction(transactionId);
    // }

    // @ParameterizedTest
    // @CsvSource({
    //     "ACC123, 2",
    //     "ACC456, 1",
    //     "ACC789, 3"
    // })
    // void testGetAccountTransactions_Success(String accountId, int expectedCount) throws Exception {
    //     // Arrange
    //     List<Transaction> mockTransactions = Arrays.asList(
    //         createMockTransaction("TXN1", accountId, Currency.EUR, new BigDecimal("100.00"), TransactionDirection.IN),
    //         createMockTransaction("TXN2", accountId, Currency.SEK, new BigDecimal("200.00"), TransactionDirection.OUT)
    //     );
        
    //     when(transactionService.getAccountTransactions(accountId)).thenReturn(mockTransactions);

    //     // Act & Assert
    //     mockMvc.perform(get("/transactions/account/" + accountId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$").isArray())
    //             .andExpect(jsonPath("$.length()").value(expectedCount));

    //     verify(transactionService, times(1)).getAccountTransactions(accountId);
    // }

    // Helper methods
    private Transaction createMockTransaction(String transactionId, String accountId, Currency currency, BigDecimal amount, TransactionDirection direction) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setBalanceId(UUID.randomUUID().toString());
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDirection(direction);
        transaction.setDescription("Test transaction");
        transaction.setBalanceAfterTransaction(amount);
        transaction.setStatus(com.tuum.common.types.TransactionStatus.COMPLETED);
        transaction.setIdempotencyKey("test-key-" + transactionId);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        return transaction;
    }

    private TransactionResponse createMockTransactionResponse(String accountId, BigDecimal amount, Currency currency, TransactionDirection direction, String description) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("TXN123");
        response.setAccountId(accountId);
        response.setBalanceId("BAL123");
        response.setAmount(amount);
        response.setCurrency(currency);
        response.setDirection(direction);
        response.setDescription(description);
        response.setBalanceAfterTransaction(amount);
        response.setStatus("COMPLETED");
        response.setIdempotencyKey("test-txn-key-123");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}