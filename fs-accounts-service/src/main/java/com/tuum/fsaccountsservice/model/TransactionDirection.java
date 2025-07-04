package com.tuum.fsaccountsservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

public enum TransactionDirection {
    @Schema(description = "Money coming into the account")
    IN,
    @Schema(description = "Money going out of the account")
    OUT
} 