package com.tuum.fsaccountsservice.model;

import com.tuum.fsaccountsservice.model.TransactionDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Transaction entity")
public class Transaction {
    @Schema(description = "Unique transaction ID", example = "a9cf3df3-2a6f-41b5-8825-815dd29a7fcd")
    private String transactionId;
    
    @Schema(description = "Account ID", example = "ACCD92D2D8E")
    private String accountId;
    
    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;
    
    @Schema(description = "Transaction currency", example = "EUR")
    private String currency;
    
    @Schema(description = "Transaction direction", example = "IN")
    private TransactionDirection direction;
    
    @Schema(description = "Transaction description", example = "Payment for invoice #1234")
    private String description;
    
    @Schema(description = "Account balance after the transaction", example = "500.75")
    private BigDecimal balanceAfterTransaction;
    
    @Schema(description = "Transaction status", example = "COMPLETED")
    private String status;
    
    @Schema(description = "Idempotency key used for the request", example = "unique-key-123")
    private String idempotencyKey;
    
    @Schema(description = "Transaction creation timestamp", example = "2025-07-04 14:39:10.162419")
    private String createdAt;
} 