package com.tuum.fsaccountsservice.dto.requests;

import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Request body for creating a transaction")
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    
    @Schema(
        description = "Account ID to perform the transaction on", 
        example = "ACCD92D2D8E", 
        required = true
    )
    @NotBlank(message = "Account ID is mandatory and cannot be empty")
    @Size(min = 1, max = 50, message = "Account ID must be between 1 and 50 characters")
    private String accountId;

    @Schema(
        description = "Transaction amount", 
        example = "100.50", 
        required = true
    )
    @NotNull(message = "Amount is mandatory and cannot be null")
    @Positive(message = "Amount must be positive and greater than zero")
    private BigDecimal amount;

    @Schema(
        description = "Transaction currency", 
        example = "EUR", 
        required = true
    )
    @NotNull(message = "Currency is mandatory and cannot be null")
    private Currency currency;

    @Schema(
        description = "Transaction direction (IN or OUT)", 
        example = "IN", 
        required = true
    )
    @NotNull(message = "Direction is mandatory and cannot be null")
    private TransactionDirection direction;

    @Schema(
        description = "Transaction description", 
        example = "Payment for invoice #1234", 
        required = true
    )
    @NotBlank(message = "Description is mandatory and cannot be empty")
    @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters")
    private String description;
} 