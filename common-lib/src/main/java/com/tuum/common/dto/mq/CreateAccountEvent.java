package com.tuum.common.dto.mq;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.tuum.common.domain.entities.Balance;

import java.time.LocalDateTime;
import java.util.List;

@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
public class CreateAccountEvent {
    private String requestId;
    private String accountId;
    private String customerId;
    private String country;
    private String idempotencyKey;
    private List<Balance> balances;

    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime processedAt;

    // Default constructor
    public CreateAccountEvent() {}

    // Full constructor
    public CreateAccountEvent(String requestId, String accountId, String customerId, String country,
                              String idempotencyKey, List<Balance> balances,
                              LocalDateTime updatedAt, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.requestId = requestId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.country = country;
        this.idempotencyKey = idempotencyKey;
        this.balances = balances;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public List<Balance> getBalances() { return balances; }
    public void setBalances(List<Balance> balances) { this.balances = balances; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    // Builder pattern
    public static class Builder {
        private String requestId;
        private String accountId;
        private String customerId;
        private String country;
        private String idempotencyKey;
        private List<Balance> balances;
        private LocalDateTime updatedAt;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder balances(List<Balance> balances) {
            this.balances = balances;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public CreateAccountEvent build() {
            return new CreateAccountEvent(requestId, accountId, customerId, country, idempotencyKey,
                    balances, updatedAt, createdAt, processedAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}