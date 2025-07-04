package com.tuum.fsaccountsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body for a processed transaction")
public class TransactionProcessedEvent {
    @Schema(description = "Unique transaction ID", example = "a9cf3df3-2a6f-41b5-8825-815dd29a7fcd")
    private String transactionId;
    
    @Schema(description = "Account ID", example = "ACCD92D2D8E")
    private String accountId;
    
    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;
    
    @Schema(description = "Transaction currency", example = "EUR")
    private String currency;
    
    @Schema(description = "Transaction direction (IN or OUT)", example = "IN")
    private String direction;
    
    @Schema(description = "Transaction description", example = "Payment for invoice #1234")
    private String description;
    
    @Schema(description = "Idempotency key used for the request", example = "unique-key-123")
    private String idempotencyKey;
    
    @Schema(description = "Account balance after the transaction", example = "500.75")
    private BigDecimal balanceAfterTransaction;
    
    @Schema(description = "Transaction status", example = "COMPLETED")
    private String status;
    
    @Schema(description = "Error message if transaction failed", example = "Insufficient funds")
    private String errorMessage;
    
    @Schema(description = "Timestamp when transaction was processed", example = "2025-07-04T14:39:10.165157380")
    private String processedAt;
} 