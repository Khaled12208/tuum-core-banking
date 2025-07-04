package com.tuum.fsaccountsservice.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Balance {
    private String balanceId;
    private String accountId;
    private String currency;
    private BigDecimal availableAmount;
    private Integer versionNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 