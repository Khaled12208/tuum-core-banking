package com.tuum.fsaccountsservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountCreatedEvent {
    private String accountId;
    private String requestId;
    private String customerId;
    private String country;
    private List<String> currencies;
    private String createdAt;

    // Constructors
    public AccountCreatedEvent() {}

    public AccountCreatedEvent(String requestId, String customerId, String country, List<String> currencies, String createdAt) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.country = country;
        this.currencies = currencies;
        this.createdAt = createdAt;
    }

    public AccountCreatedEvent(String accountId, String requestId, String customerId, String country, List<String> currencies, String createdAt) {
        this.accountId = accountId;
        this.requestId = requestId;
        this.customerId = customerId;
        this.country = country;
        this.currencies = currencies;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AccountCreatedEvent{" +
                "accountId='" + accountId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", country='" + country + '\'' +
                ", currencies=" + currencies +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
} 