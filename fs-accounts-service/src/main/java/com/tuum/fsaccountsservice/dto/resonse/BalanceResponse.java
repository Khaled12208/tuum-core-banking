package com.tuum.fsaccountsservice.dto.resonse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuum.common.types.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Balance information for a specific currency")
public  class BalanceResponse {

    @Schema(description = "Unique balance identifier", example = "BAL12345678")
    private String balanceId;
    
    @Schema(description = "Account identifier", example = "ACC12345678")
    private String accountId;
    
    @Schema(description = "Currency of the balance", example = "EUR")
    private Currency currency;
    
    @Schema(description = "Available amount in the balance", example = "1000.50")
    private BigDecimal availableAmount;
    
    @Schema(description = "Balance creation timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last balance update timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;


} 