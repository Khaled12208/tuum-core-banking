package com.tuum.common.exception;

public class InsufficientFundsException extends BusinessException {
    
    public InsufficientFundsException(String accountId, String currency) {
        super(String.format("Insufficient funds in account %s for currency %s", accountId, currency),
              "INSUFFICIENT_FUNDS");
    }
    
    public InsufficientFundsException(String message) {
        super(message, "INSUFFICIENT_FUNDS");
    }
} 