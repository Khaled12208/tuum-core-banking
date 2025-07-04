package com.tuum.fsaccountsservice.exception;

public class InsufficientFundsException extends BusinessException {
    
    public InsufficientFundsException(String accountId, String currency) {
        super(String.format("Insufficient funds in account %s for currency %s", accountId, currency),
              "INSUFFICIENT_FUNDS", 400);
    }
    
    public InsufficientFundsException(String message) {
        super(message, "INSUFFICIENT_FUNDS", 400);
    }
} 