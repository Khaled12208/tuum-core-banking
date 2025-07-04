package com.tuum.csaccountseventsconsumer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String direction;
    private String description;
    private String idempotencyKey;
    private BigDecimal balanceAfterTransaction;
    private String createdAt;
} 