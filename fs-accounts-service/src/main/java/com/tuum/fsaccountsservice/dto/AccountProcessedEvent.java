package com.tuum.fsaccountsservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountProcessedEvent {
    private String accountId;
    private String customerId;
    private String country;
    private List<String> currencies;
    private List<BalanceInfo> balances;
    private String status;
    private String errorMessage;
    private String originalMessage;
    private String requestId;
    
    private String processedAt;
    
    private String type = "ACCOUNT_PROCESSED";
    
    @Data
    public static class BalanceInfo {
        private String currency;
        private BigDecimal availableAmount;
    }
} 