package com.tuum.fsaccountsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import com.tuum.fsaccountsservice.model.Currency;
import com.tuum.fsaccountsservice.model.TransactionDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Schema(description = "Request body for creating a transaction")
public class CreateTransactionRequest {
    @Schema(description = "Account ID to perform the transaction on", example = "ACCD92D2D8E", required = true)
    @NotBlank(message = "accountId is mandatory")
    private String accountId;

    @Schema(description = "Transaction amount", example = "100.50", required = true)
    @NotNull(message = "amount is mandatory")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Transaction currency", example = "EUR", required = true)
    @NotNull(message = "currency is mandatory")
    private Currency currency;

    @Schema(description = "Transaction direction (IN or OUT)", example = "IN", required = true)
    @NotNull(message = "direction is mandatory")
    private TransactionDirection direction;

    @Schema(description = "Transaction description", example = "Payment for invoice #1234", required = true)
    @NotBlank(message = "description is mandatory")
    private String description;
} 