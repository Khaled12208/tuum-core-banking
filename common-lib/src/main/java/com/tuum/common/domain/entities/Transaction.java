package com.tuum.common.domain.entities;

import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import com.tuum.common.types.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {
    private String transactionId;
    private String accountId;
    private String balanceId;
    private BigDecimal amount;
    private Currency currency;
    private TransactionDirection direction;
    private String description;
    private BigDecimal balanceAfterTransaction;
    private TransactionStatus status;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 