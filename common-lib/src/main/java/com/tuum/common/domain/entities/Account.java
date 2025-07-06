package com.tuum.common.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String accountId;
    private String customerId;
    private String country;
    private String idempotencyKey;
    private List<Balance> balances;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}