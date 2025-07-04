package com.tuum.fsaccountsservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateAccountResponse {
    private String accountId;
    private String customerId;
    private String country;
    private List<String> currencies;
} 