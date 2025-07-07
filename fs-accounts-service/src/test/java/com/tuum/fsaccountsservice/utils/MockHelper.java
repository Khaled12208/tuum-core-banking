package com.tuum.fsaccountsservice.utils;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.mapper.BalanceMapper;
import com.tuum.fsaccountsservice.mapper.TransactionMapper;
import com.tuum.fsaccountsservice.service.AccountService;
import com.tuum.fsaccountsservice.service.TransactionService;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import com.tuum.common.types.TransactionStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MockHelper utility class following Single Responsibility Principle.
 * Centralizes mock creation and verification logic for unit tests.
 */
public final class MockHelper {

    // Private constructor to prevent instantiation
    private MockHelper() {}

    /**
     * Sets up AccountService mocks for successful account creation
     */
    public static void setupAccountServiceCreateSuccess(AccountService accountService, CreateAccountRequest request, AccountResponse response) {
        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenReturn(response);
    }

    /**
     * Sets up AccountService mocks for successful account retrieval
     */
    public static void setupAccountServiceGetSuccess(AccountService accountService, String accountId, Account account) {
        when(accountService.getAccount(accountId)).thenReturn(account);
    }

    /**
     * Sets up AccountService mocks for account not found scenario
     */
    public static void setupAccountServiceGetNotFound(AccountService accountService, String accountId) {
        when(accountService.getAccount(accountId))
                .thenThrow(new ResourceNotFoundException(TestConstants.ACCOUNT_NOT_FOUND_MESSAGE + accountId));
    }

    /**
     * Sets up AccountService mocks for successful customer accounts retrieval
     */
    public static void setupAccountServiceGetByCustomerSuccess(AccountService accountService, String customerId, List<Account> accounts) {
        when(accountService.getAccountsByCustomerId(customerId)).thenReturn(accounts);
    }

    /**
     * Sets up AccountService mocks for successful all accounts retrieval
     */
    public static void setupAccountServiceGetAllSuccess(AccountService accountService, List<Account> accounts) {
        when(accountService.getAllAccounts()).thenReturn(accounts);
    }

    /**
     * Sets up TransactionService mocks for successful transaction creation
     */
    public static void setupTransactionServiceCreateSuccess(TransactionService transactionService, CreateTransactionRequest request, TransactionResponse response) {
        when(transactionService.createTransaction(any(CreateTransactionRequest.class), anyString()))
                .thenReturn(response);
    }

    /**
     * Sets up TransactionService mocks for successful transaction retrieval
     */
    public static void setupTransactionServiceGetSuccess(TransactionService transactionService, String transactionId, Transaction transaction) {
        when(transactionService.getTransaction(transactionId)).thenReturn(transaction);
    }

    /**
     * Sets up TransactionService mocks for transaction not found scenario
     */
    public static void setupTransactionServiceGetNotFound(TransactionService transactionService, String transactionId) {
        when(transactionService.getTransaction(transactionId))
                .thenThrow(new ResourceNotFoundException(TestConstants.TRANSACTION_NOT_FOUND_MESSAGE + transactionId));
    }

    /**
     * Sets up TransactionService mocks for successful account transactions retrieval
     */
    public static void setupTransactionServiceGetByAccountSuccess(TransactionService transactionService, String accountId, List<Transaction> transactions) {
        when(transactionService.getAccountTransactions(accountId)).thenReturn(transactions);
    }

    /**
     * Sets up AccountMapper mocks for successful account retrieval
     */
    public static void setupAccountMapperFindSuccess(AccountMapper accountMapper, String accountId, Account account) {
        when(accountMapper.findAccountById(accountId)).thenReturn(account);
    }

    /**
     * Sets up AccountMapper mocks for account not found scenario
     */
    public static void setupAccountMapperFindNotFound(AccountMapper accountMapper, String accountId) {
        when(accountMapper.findAccountById(accountId)).thenReturn(null);
    }

    /**
     * Sets up AccountMapper mocks for successful customer accounts retrieval
     */
    public static void setupAccountMapperFindByCustomerSuccess(AccountMapper accountMapper, String customerId, List<Account> accounts) {
        when(accountMapper.findAccountsByCustomerId(customerId)).thenReturn(accounts);
    }

    /**
     * Sets up AccountMapper mocks for successful all accounts retrieval
     */
    public static void setupAccountMapperFindAllSuccess(AccountMapper accountMapper, List<Account> accounts) {
        when(accountMapper.findAllAccounts()).thenReturn(accounts);
    }

    /**
     * Sets up BalanceMapper mocks for successful balance retrieval
     */
    public static void setupBalanceMapperFindSuccess(BalanceMapper balanceMapper, String accountId, List<Balance> balances) {
        when(balanceMapper.findBalancesByAccountId(accountId)).thenReturn(balances);
    }

    /**
     * Sets up TransactionMapper mocks for successful transaction retrieval
     */
    public static void setupTransactionMapperFindSuccess(TransactionMapper transactionMapper, String transactionId, Transaction transaction) {
        when(transactionMapper.findTransactionById(transactionId)).thenReturn(transaction);
    }

    /**
     * Sets up TransactionMapper mocks for transaction not found scenario
     */
    public static void setupTransactionMapperFindNotFound(TransactionMapper transactionMapper, String transactionId) {
        when(transactionMapper.findTransactionById(transactionId)).thenReturn(null);
    }

    /**
     * Sets up TransactionMapper mocks for successful account transactions retrieval
     */
    public static void setupTransactionMapperFindByAccountSuccess(TransactionMapper transactionMapper, String accountId, List<Transaction> transactions) {
        when(transactionMapper.findTransactionsByAccountId(accountId)).thenReturn(transactions);
    }

    /**
     * Verifies AccountService createAccount was called once
     */
    public static void verifyAccountServiceCreateCalledOnce(AccountService accountService, String idempotencyKey) {
        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), eq(idempotencyKey));
    }

    /**
     * Verifies AccountService getAccount was called once
     */
    public static void verifyAccountServiceGetCalledOnce(AccountService accountService, String accountId) {
        verify(accountService, times(1)).getAccount(accountId);
    }

    /**
     * Verifies AccountService getAccountsByCustomerId was called once
     */
    public static void verifyAccountServiceGetByCustomerCalledOnce(AccountService accountService, String customerId) {
        verify(accountService, times(1)).getAccountsByCustomerId(customerId);
    }

    /**
     * Verifies AccountService getAllAccounts was called once
     */
    public static void verifyAccountServiceGetAllCalledOnce(AccountService accountService) {
        verify(accountService, times(1)).getAllAccounts();
    }

    /**
     * Verifies TransactionService createTransaction was called once
     */
    public static void verifyTransactionServiceCreateCalledOnce(TransactionService transactionService, String idempotencyKey) {
        verify(transactionService, times(1)).createTransaction(any(CreateTransactionRequest.class), eq(idempotencyKey));
    }

    /**
     * Verifies TransactionService getTransaction was called once
     */
    public static void verifyTransactionServiceGetCalledOnce(TransactionService transactionService, String transactionId) {
        verify(transactionService, times(1)).getTransaction(transactionId);
    }

    /**
     * Verifies TransactionService getAccountTransactions was called once
     */
    public static void verifyTransactionServiceGetByAccountCalledOnce(TransactionService transactionService, String accountId) {
        verify(transactionService, times(1)).getAccountTransactions(accountId);
    }

    /**
     * Verifies AccountMapper findAccountById was called once
     */
    public static void verifyAccountMapperFindCalledOnce(AccountMapper accountMapper, String accountId) {
        verify(accountMapper, times(1)).findAccountById(accountId);
    }

    /**
     * Verifies AccountMapper findAccountsByCustomerId was called once
     */
    public static void verifyAccountMapperFindByCustomerCalledOnce(AccountMapper accountMapper, String customerId) {
        verify(accountMapper, times(1)).findAccountsByCustomerId(customerId);
    }

    /**
     * Verifies AccountMapper findAllAccounts was called once
     */
    public static void verifyAccountMapperFindAllCalledOnce(AccountMapper accountMapper) {
        verify(accountMapper, times(1)).findAllAccounts();
    }

    /**
     * Verifies BalanceMapper findBalancesByAccountId was called once
     */
    public static void verifyBalanceMapperFindCalledOnce(BalanceMapper balanceMapper, String accountId) {
        verify(balanceMapper, times(1)).findBalancesByAccountId(accountId);
    }

    /**
     * Verifies TransactionMapper findTransactionById was called once
     */
    public static void verifyTransactionMapperFindCalledOnce(TransactionMapper transactionMapper, String transactionId) {
        verify(transactionMapper, times(1)).findTransactionById(transactionId);
    }

    /**
     * Verifies TransactionMapper findTransactionsByAccountId was called once
     */
    public static void verifyTransactionMapperFindByAccountCalledOnce(TransactionMapper transactionMapper, String accountId) {
        verify(transactionMapper, times(1)).findTransactionsByAccountId(accountId);
    }

    /**
     * Creates a mock Account with default values
     */
    public static Account createMockAccount(String accountId, String customerId, String country) {
        return TestDataBuilder.account()
                .accountId(accountId)
                .customerId(customerId)
                .country(country)
                .build();
    }

    /**
     * Creates a mock Balance with default values
     */
    public static Balance createMockBalance(String accountId, Currency currency, BigDecimal amount) {
        return TestDataBuilder.balance()
                .accountId(accountId)
                .currency(currency)
                .availableAmount(amount)
                .build();
    }

    /**
     * Creates a mock Transaction with default values
     */
    public static Transaction createMockTransaction(String transactionId, String accountId, Currency currency, BigDecimal amount, TransactionDirection direction) {
        return TestDataBuilder.transaction()
                .transactionId(transactionId)
                .accountId(accountId)
                .currency(currency)
                .amount(amount)
                .direction(direction)
                .build();
    }

    /**
     * Creates a mock AccountResponse with default values
     */
    public static AccountResponse createMockAccountResponse(String accountId, String customerId, String country) {
        return TestDataBuilder.accountResponse()
                .accountId(accountId)
                .customerId(customerId)
                .country(country)
                .build();
    }

    /**
     * Creates a mock TransactionResponse with default values
     */
    public static TransactionResponse createMockTransactionResponse(String transactionId, String accountId, Currency currency, BigDecimal amount, TransactionDirection direction) {
        return TestDataBuilder.transactionResponse()
                .transactionId(transactionId)
                .accountId(accountId)
                .currency(currency)
                .amount(amount)
                .direction(direction)
                .build();
    }
} 