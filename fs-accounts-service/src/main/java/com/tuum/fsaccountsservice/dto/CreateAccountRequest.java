package com.tuum.fsaccountsservice.dto;

import com.tuum.fsaccountsservice.model.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "Request to create a new customer account")
public class CreateAccountRequest {
    
    @Schema(
        description = "Unique identifier for the customer",
        example = "CUST001",
        minLength = 1,
        maxLength = 50
    )
    @NotBlank(message = "Customer ID is required")
    @Size(min = 1, max = 50, message = "Customer ID must be between 1 and 50 characters")
    private String customerId;
    
    @Schema(
        description = "2-letter ISO country code where the account is opened",
        example = "EE",
        pattern = "^[A-Z]{2}$"
    )
    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a 2-letter ISO country code")
    private String country;
    
    @Schema(
        description = "List of currencies supported by this account (1-10 currencies)",
        example = "[\"EUR\", \"USD\"]"
    )
    @NotEmpty(message = "At least one currency is required")
    @Size(min = 1, max = 10, message = "Number of currencies must be between 1 and 10")
    private List<Currency> currencies;
} 