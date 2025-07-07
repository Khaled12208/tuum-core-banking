package com.tuum.fsaccountsservice.controller;

import com.tuum.common.domain.entities.Transaction;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.exception.InsufficientFundsException;
import com.tuum.common.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.service.TransactionService;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;
import com.tuum.common.dto.ErrorResponse;
import com.tuum.common.validation.ValidIdempotencyKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new financial transaction (deposit or withdrawal) for an account. Requires idempotency key to prevent duplicate processing."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "transactionId": "TXN12345678",
                        "accountId": "ACC12345678",
                        "balanceId": "BAL12345678",
                        "amount": 100.50,
                        "currency": "EUR",
                        "direction": "IN",
                        "description": "Payment for invoice #1234",
                        "balanceAfterTransaction": 1100.50,
                        "status": "COMPLETED",
                        "idempotencyKey": "req-123456",
                        "balance": {
                            "balanceId": "BAL12345678",
                            "accountId": "ACC12345678",
                            "currency": "EUR",
                            "availableAmount": 1100.50,
                            "createdAt": "2024-01-15T10:30:00",
                            "updatedAt": "2024-01-15T10:35:00"
                        },
                        "createdAt": "2024-01-15T10:35:00",
                        "updatedAt": "2024-01-15T10:35:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation error or insufficient funds",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Account or balance not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - transaction already exists or duplicate request",
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
    public ResponseEntity<TransactionResponse> createTransaction(
            @Parameter(description = "Transaction creation request", required = true)
            @Valid @RequestBody CreateTransactionRequest request,
            @Parameter(description = "Unique key to prevent duplicate processing", required = true, example = "req-123456")
            @ValidIdempotencyKey @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        
        log.info("Creating transaction for account: {}", request.getAccountId());

        try {
            TransactionResponse result = transactionService.createTransaction(request, idempotencyKey);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds for transaction: {}", e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.error("Business error creating transaction: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating transaction: {}", e.getMessage(), e);
            throw new BusinessException("Failed to create transaction: " + e.getMessage());
        }
    }

    @GetMapping("/account/{accountId}")
    @Operation(
        summary = "Get transactions by account ID",
        description = "Retrieves all transactions for a specific account"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transactions found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
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
    public ResponseEntity<List<Transaction>> getAccountTransactions(
            @Parameter(description = "Account identifier", required = true, example = "ACC12345678")
            @PathVariable String accountId) {
        log.info("Retrieving transactions for account: {}", accountId);
        
        try {
            List<Transaction> transactions = transactionService.getAccountTransactions(accountId);
            return ResponseEntity.ok(transactions);
        } catch (ResourceNotFoundException e) {
            log.warn("Account not found: {}", accountId);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving transactions for account: {}", accountId, e);
            throw new BusinessException("Failed to retrieve transactions: " + e.getMessage());
        }
    }

    @GetMapping("/{transactionId}")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves a specific transaction by its identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Transaction> getTransaction(
            @Parameter(description = "Transaction identifier", required = true, example = "TXN12345678")
            @PathVariable String transactionId) {
        log.info("Retrieving transaction: {}", transactionId);
        
        try {
            Transaction transaction = transactionService.getTransaction(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (ResourceNotFoundException e) {
            log.warn("Transaction not found: {}", transactionId);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving transaction: {}", transactionId, e);
            throw new BusinessException("Failed to retrieve transaction: " + e.getMessage());
        }
    }
} 