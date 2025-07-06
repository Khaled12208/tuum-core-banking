package com.tuum.common.domain.entities;

import com.tuum.common.types.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {
    private String balanceId;
    private String accountId;
    private Currency currency;
    private BigDecimal availableAmount;
    private Integer versionNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 