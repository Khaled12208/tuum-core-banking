package com.tuum.csaccountseventsconsumer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String direction;
    private String description;
    private BigDecimal balanceAfter;
    private String status;
    private String idempotencyKey;
    private LocalDateTime createdAt;
} 