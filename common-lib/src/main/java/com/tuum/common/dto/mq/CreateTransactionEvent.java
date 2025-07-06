package com.tuum.common.dto.mq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
public class CreateTransactionEvent {
    private String requestId;
    private String idempotencyKey;
    private String transactionId;
    private String accountId;
    private String balanceId;
    private BigDecimal amount;
    private Currency currency;
    private TransactionDirection direction;
    private String description;
    private BigDecimal balanceAfterTransaction;
    private String status;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime processedAt;

    public CreateTransactionEvent() {}

    public CreateTransactionEvent(String requestId, String idempotencyKey, String transactionId, String accountId,
                                  String balanceId, BigDecimal amount, Currency currency, TransactionDirection direction,
                                  String description, BigDecimal balanceAfterTransaction, String status,
                                  String errorMessage, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime processedAt) {
        this.requestId = requestId;
        this.idempotencyKey = idempotencyKey;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.balanceId = balanceId;
        this.amount = amount;
        this.currency = currency;
        this.direction = direction;
        this.description = description;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(String balanceId) {
        this.balanceId = balanceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public void setDirection(TransactionDirection direction) {
        this.direction = direction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private String requestId;
        private String idempotencyKey;
        private String transactionId;
        private String accountId;
        private String balanceId;
        private BigDecimal amount;
        private Currency currency;
        private TransactionDirection direction;
        private String description;
        private BigDecimal balanceAfterTransaction;
        private String status;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime processedAt;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder balanceId(String balanceId) {
            this.balanceId = balanceId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public Builder direction(TransactionDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder balanceAfterTransaction(BigDecimal balanceAfterTransaction) {
            this.balanceAfterTransaction = balanceAfterTransaction;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public CreateTransactionEvent build() {
            return new CreateTransactionEvent(requestId, idempotencyKey, transactionId, accountId, balanceId,
                    amount, currency, direction, description, balanceAfterTransaction, status,
                    errorMessage, createdAt, updatedAt, processedAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
