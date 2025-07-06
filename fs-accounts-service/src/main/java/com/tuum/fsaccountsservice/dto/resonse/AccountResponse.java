package com.tuum.fsaccountsservice.dto.resonse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuum.common.domain.entities.Balance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account information response")
public class AccountResponse {
    @Schema(description = "Unique account identifier", example = "ACC12345678")
    private String accountId;
    
    @Schema(description = "Customer identifier", example = "CUST001")
    private String customerId;
    
    @Schema(description = "2-letter ISO country code", example = "EE")
    private String country;
    
    @Schema(description = "List of balances for different currencies")
    private List<BalanceResponse> balances;
    
    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;


} 