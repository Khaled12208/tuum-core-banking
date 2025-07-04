package com.tuum.fsaccountsservice.controller;

import com.tuum.fsaccountsservice.dto.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.TransactionProcessedEvent;
import com.tuum.fsaccountsservice.exception.BusinessException;
import com.tuum.fsaccountsservice.model.Transaction;
import com.tuum.fsaccountsservice.service.TransactionService;
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

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Endpoints for transaction management")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a transaction asynchronously and waits for completion
     * Handles concurrent transactions and prevents duplicate submissions using idempotency keys
     */
    @Operation(summary = "Create a new transaction", description = "Creates a transaction and waits for processing. Handles idempotency and concurrency.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully", content = @Content(schema = @Schema(implementation = TransactionProcessedEvent.class))),
        @ApiResponse(responseCode = "400", description = "Business or validation error", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionProcessedEvent> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transaction creation request", required = true, content = @Content(schema = @Schema(implementation = CreateTransactionRequest.class)))
            @Valid @RequestBody CreateTransactionRequest request,
            @Parameter(description = "Idempotency key for safe retries", required = false, example = "unique-key-123")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        log.info("Received request to create transaction for account: {}", request.getAccountId());
        
        // Generate idempotency key if not provided
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            idempotencyKey = UUID.randomUUID().toString();
            log.info("Generated idempotency key: {}", idempotencyKey);
        }
        
        try {
            TransactionProcessedEvent result = transactionService.createTransaction(request, idempotencyKey);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error creating transaction: {}", e.getMessage(), e);
            throw new BusinessException("Failed to create transaction: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all transactions for an account", description = "Returns all transactions for a given account ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of transactions", content = @Content(schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "400", description = "Business or validation error", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(
            @Parameter(description = "Account ID", required = true, example = "ACCD92D2D8E")
            @PathVariable String accountId) {
        log.info("Getting transactions for account: {}", accountId);
        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
            if (transactions == null) {
                return ResponseEntity.ok(List.of()); // Return empty list instead of null
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error retrieving transactions for account: {}", accountId, e);
            throw new BusinessException("Failed to retrieve transactions: " + e.getMessage());
        }
    }

    @Operation(summary = "Get a transaction by ID", description = "Returns a single transaction by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found", content = @Content(schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Business or validation error", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(
            @Parameter(description = "Transaction ID", required = true, example = "a9cf3df3-2a6f-41b5-8825-815dd29a7fcd")
            @PathVariable String transactionId) {
        log.info("Getting transaction by ID: {}", transactionId);
        try {
            Transaction transaction = transactionService.getTransactionById(transactionId);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error retrieving transaction: {}", transactionId, e);
            throw new BusinessException("Failed to retrieve transaction: " + e.getMessage());
        }
    }
} 