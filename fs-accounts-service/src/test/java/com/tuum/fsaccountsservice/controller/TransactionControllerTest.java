package com.tuum.fsaccountsservice.controller;

import com.tuum.fsaccountsservice.model.Transaction;
import com.tuum.fsaccountsservice.model.TransactionDirection;
import com.tuum.fsaccountsservice.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@TestPropertySource(properties = {
    "server.servlet.context-path=/api/v1"
})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testGetTransaction_returnsTransaction() throws Exception {
        // Arrange
        String transactionId = "TXN123";
        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(transactionId);
        mockTransaction.setAccountId("ACC123");
        mockTransaction.setAmount(new BigDecimal("100.50"));
        mockTransaction.setCurrency("EUR");
        mockTransaction.setDirection(TransactionDirection.IN);
        mockTransaction.setDescription("Deposit");
        mockTransaction.setStatus("COMPLETED");
        mockTransaction.setCreatedAt("2025-07-04 14:39:10.162419");
        when(transactionService.getTransactionById(transactionId)).thenReturn(mockTransaction);

        // Act & Assert
        mockMvc.perform(get("/transactions/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.accountId").value("ACC123"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.direction").value("IN"))
                .andExpect(jsonPath("$.description").value("Deposit"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
} 