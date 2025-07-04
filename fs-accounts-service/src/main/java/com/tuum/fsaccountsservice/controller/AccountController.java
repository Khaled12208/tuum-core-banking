package com.tuum.fsaccountsservice.controller;

import com.tuum.fsaccountsservice.dto.AccountProcessedEvent;
import com.tuum.fsaccountsservice.dto.CreateAccountRequest;
import com.tuum.fsaccountsservice.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.model.Account;
import com.tuum.fsaccountsservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "Endpoints for account management")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new account", description = "Creates a new account with specified currencies and waits for processing. Handles idempotency and concurrency.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully", content = @Content(schema = @Schema(implementation = AccountProcessedEvent.class))),
        @ApiResponse(responseCode = "400", description = "Business or validation error", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AccountProcessedEvent> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Account creation request", required = true, content = @Content(schema = @Schema(implementation = CreateAccountRequest.class)))
            @RequestBody CreateAccountRequest request,
            @Parameter(description = "Idempotency key for safe retries", required = false, example = "unique-key-123")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received request to create account for customer: {}", request.getCustomerId());
        AccountProcessedEvent result = accountService.createAccount(request, idempotencyKey);
        log.info("Account creation result: {}", result);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Get account by ID", description = "Returns a single account by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found", content = @Content(schema = @Schema(implementation = Account.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(
            @Parameter(description = "Account ID", required = true, example = "ACCD92D2D8E")
            @PathVariable String accountId) {
        log.info("Received request to get account: {}", accountId);
        Account account = accountService.getAccount(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found with ID: " + accountId);
        }
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Get accounts by customer ID", description = "Returns all accounts for a given customer ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of accounts", content = @Content(schema = @Schema(implementation = Account.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomerId(
            @Parameter(description = "Customer ID", required = true, example = "CUST001")
            @PathVariable String customerId) {
        log.info("Received request to get accounts for customer: {}", customerId);
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get all accounts", description = "Returns all accounts in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of all accounts", content = @Content(schema = @Schema(implementation = Account.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        log.info("Received request to get all accounts");
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Search accounts by currency and account ID", description = "Returns accounts filtered by currency and account ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of filtered accounts", content = @Content(schema = @Schema(implementation = Account.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<List<Account>> getAccountsByCurrencyAndAccountId(
            @Parameter(description = "Currency to filter by", required = true, example = "EUR")
            @RequestParam String currency,
            @Parameter(description = "Account ID to filter by", required = true, example = "ACCD92D2D8E")
            @RequestParam String accountId) {
        log.info("Received request to get accounts filtered by currency: {} and accountId: {}", currency, accountId);
        List<Account> accounts = accountService.getAccountsByCurrencyAndAccountId(currency, accountId);
        return ResponseEntity.ok(accounts);
    }
} 