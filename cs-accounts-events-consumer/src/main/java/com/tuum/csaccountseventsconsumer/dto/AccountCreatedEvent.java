package com.tuum.csaccountseventsconsumer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountCreatedEvent {
    private String accountId;
    private String requestId;
    private String customerId;
    private String country;
    private List<String> currencies;
    private String createdAt;
    
    public AccountCreatedEvent(String requestId, String customerId, String country, List<String> currencies, String createdAt) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.country = country;
        this.currencies = currencies;
        this.createdAt = createdAt;
    }
} 