package com.tuum.fsaccountsservice.controller;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.fsaccountsservice.util.DtoMapper;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.dto.ErrorResponse;
import com.tuum.common.validation.ValidIdempotencyKey;

import com.tuum.fsaccountsservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing customer accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(
        summary = "Create a new account",
        description = "Creates a new customer account with specified currencies. Requires idempotency key to prevent duplicate processing."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Account created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "accountId": "ACC12345678",
                        "customerId": "CUST001",
                        "country": "EE",
                        "balances": [
                            {
                                "balanceId": "BAL12345678",
                                "accountId": "ACC12345678",
                                "currency": "EUR",
                                "availableAmount": 0.00,
                                "createdAt": "2024-01-15T10:30:00",
                                "updatedAt": "2024-01-15T10:30:00"
                            }
                        ],
                        "createdAt": "2024-01-15T10:30:00",
                        "updatedAt": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - account already exists or duplicate request",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "IdempotencyKey")
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AccountResponse> createAccount(
            @Parameter(description = "Account creation request", required = true)
            @Valid @RequestBody CreateAccountRequest request,
            @Parameter(description = "Unique key to prevent duplicate processing", required = true, example = "req-123456")
            @ValidIdempotencyKey @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        log.info("Received request to create account for customer: {}", request.getCustomerId());
        AccountResponse result = accountService.createAccount(request, idempotencyKey);
        log.info("Account creation result: {}", result);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{accountId}")
    @Operation(
        summary = "Get account by ID",
        description = "Retrieves account information including balances for all currencies"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Account found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Account not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "Account identifier", required = true, example = "ACC12345678")
            @PathVariable String accountId) {
        Account account = accountService.getAccount(accountId); // Already includes balances
        AccountResponse response = DtoMapper.toAccountResponse(account);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Get accounts by customer ID",
        description = "Retrieves all accounts for a specific customer"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Accounts found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(
            @Parameter(description = "Customer identifier", required = true, example = "CUST001")
            @PathVariable String customerId) {
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        List<AccountResponse> responses = accounts.stream()
                .map(DtoMapper::toAccountResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
    @GetMapping
    @Operation(
        summary = "Get all accounts",
        description = "Retrieves all accounts in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Accounts retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountResponse.class)
            )
        )
    })
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        List<AccountResponse> responses = accounts.stream()
                .map(DtoMapper::toAccountResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
} 