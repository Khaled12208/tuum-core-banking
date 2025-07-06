package com.tuum.fsaccountsservice.dto.resonse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction information response")
public class TransactionResponse {
    @Schema(description = "Unique transaction identifier", example = "TXN12345678")
    private String transactionId;
    
    @Schema(description = "Account identifier", example = "ACC12345678")
    private String accountId;
    
    @Schema(description = "Balance identifier", example = "BAL12345678")
    private String balanceId;
    
    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;
    
    @Schema(description = "Transaction currency", example = "EUR")
    private Currency currency;
    
    @Schema(description = "Transaction direction (IN or OUT)", example = "IN")
    private TransactionDirection direction;
    
    @Schema(description = "Transaction description", example = "Payment for invoice #1234")
    private String description;
    
    @Schema(description = "Balance amount after transaction", example = "1100.50")
    private BigDecimal balanceAfterTransaction;
    
    @Schema(description = "Transaction status", example = "COMPLETED")
    private String status;
    
    @Schema(description = "Idempotency key used for the request", example = "req-123456")
    private String idempotencyKey;
    
    @Schema(description = "Balance information after transaction")
    private BalanceResponse balance;

    @Schema(description = "Transaction creation timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last transaction update timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
} 