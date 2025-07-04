package com.tuum.fsaccountsservice.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Account {
    private String accountId;
    private String customerId;
    private String country;
    private List<String> currencies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Balance> balances;
} 