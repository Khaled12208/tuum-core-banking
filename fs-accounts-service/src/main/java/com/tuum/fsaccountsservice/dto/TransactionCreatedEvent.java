package com.tuum.fsaccountsservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionCreatedEvent {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String direction;
    private String description;
    private String idempotencyKey;
    private String createdAt;
} 