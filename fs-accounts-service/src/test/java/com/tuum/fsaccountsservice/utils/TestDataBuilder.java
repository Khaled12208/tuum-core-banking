package com.tuum.fsaccountsservice.utils;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.domain.entities.Transaction;
import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;
import com.tuum.common.types.TransactionStatus;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.requests.CreateTransactionRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.fsaccountsservice.dto.resonse.BalanceResponse;
import com.tuum.fsaccountsservice.dto.resonse.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestDataBuilder {

    private TestDataBuilder() {}

    public static class AccountBuilder {
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String customerId = "CUST" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        private String country = "EE";
        private String idempotencyKey = "idempotency-" + UUID.randomUUID().toString().substring(0, 8);
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private List<Balance> balances = Arrays.asList();

        public AccountBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public AccountBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public AccountBuilder country(String country) {
            this.country = country;
            return this;
        }

        public AccountBuilder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public AccountBuilder balances(List<Balance> balances) {
            this.balances = balances;
            return this;
        }

        public AccountBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccountBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Account build() {
            Account account = new Account();
            account.setAccountId(accountId);
            account.setCustomerId(customerId);
            account.setCountry(country);
            account.setIdempotencyKey(idempotencyKey);
            account.setCreatedAt(createdAt);
            account.setUpdatedAt(updatedAt);
            account.setBalances(balances);
            return account;
        }
    }

    public static class BalanceBuilder {
        private String balanceId = "BAL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private Currency currency = Currency.EUR;
        private BigDecimal availableAmount = BigDecimal.ZERO;
        private int versionNumber = 1;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public BalanceBuilder balanceId(String balanceId) {
            this.balanceId = balanceId;
            return this;
        }

        public BalanceBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public BalanceBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public BalanceBuilder availableAmount(BigDecimal availableAmount) {
            this.availableAmount = availableAmount;
            return this;
        }

        public BalanceBuilder versionNumber(int versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public BalanceBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BalanceBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Balance build() {
            Balance balance = new Balance();
            balance.setBalanceId(balanceId);
            balance.setAccountId(accountId);
            balance.setCurrency(currency);
            balance.setAvailableAmount(availableAmount);
            balance.setVersionNumber(versionNumber);
            balance.setCreatedAt(createdAt);
            balance.setUpdatedAt(updatedAt);
            return balance;
        }
    }

    public static class TransactionBuilder {
        private String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String balanceId = "BAL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private BigDecimal amount = BigDecimal.valueOf(100.00);
        private Currency currency = Currency.EUR;
        private TransactionDirection direction = TransactionDirection.IN;
        private String description = "Test transaction";
        private BigDecimal balanceAfterTransaction = BigDecimal.valueOf(100.00);
        private TransactionStatus status = TransactionStatus.COMPLETED;
        private String idempotencyKey = "idempotency-" + UUID.randomUUID().toString().substring(0, 8);
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public TransactionBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public TransactionBuilder balanceId(String balanceId) {
            this.balanceId = balanceId;
            return this;
        }

        public TransactionBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public TransactionBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public TransactionBuilder direction(TransactionDirection direction) {
            this.direction = direction;
            return this;
        }

        public TransactionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TransactionBuilder balanceAfterTransaction(BigDecimal balanceAfterTransaction) {
            this.balanceAfterTransaction = balanceAfterTransaction;
            return this;
        }

        public TransactionBuilder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public TransactionBuilder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public TransactionBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransactionBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Transaction build() {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(transactionId);
            transaction.setAccountId(accountId);
            transaction.setBalanceId(balanceId);
            transaction.setAmount(amount);
            transaction.setCurrency(currency);
            transaction.setDirection(direction);
            transaction.setDescription(description);
            transaction.setBalanceAfterTransaction(balanceAfterTransaction);
            transaction.setStatus(status);
            transaction.setIdempotencyKey(idempotencyKey);
            transaction.setCreatedAt(createdAt);
            transaction.setUpdatedAt(updatedAt);
            return transaction;
        }
    }

    public static class CreateAccountRequestBuilder {
        private String customerId = "CUST" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        private String country = "EE";
        private List<Currency> currencies = Arrays.asList(Currency.EUR, Currency.USD);

        public CreateAccountRequestBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public CreateAccountRequestBuilder country(String country) {
            this.country = country;
            return this;
        }

        public CreateAccountRequestBuilder currencies(List<Currency> currencies) {
            this.currencies = currencies;
            return this;
        }

        public CreateAccountRequest build() {
            CreateAccountRequest request = new CreateAccountRequest();
            request.setCustomerId(customerId);
            request.setCountry(country);
            request.setCurrencies(currencies);
            return request;
        }
    }

    /**
     * Builder for CreateTransactionRequest
     */
    public static class CreateTransactionRequestBuilder {
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private BigDecimal amount = BigDecimal.valueOf(100.00);
        private Currency currency = Currency.EUR;
        private TransactionDirection direction = TransactionDirection.IN;
        private String description = "Test transaction";

        public CreateTransactionRequestBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public CreateTransactionRequestBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public CreateTransactionRequestBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public CreateTransactionRequestBuilder direction(TransactionDirection direction) {
            this.direction = direction;
            return this;
        }

        public CreateTransactionRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateTransactionRequest build() {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setAccountId(accountId);
            request.setAmount(amount);
            request.setCurrency(currency);
            request.setDirection(direction);
            request.setDescription(description);
            return request;
        }
    }

    public static class AccountResponseBuilder {
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String customerId = "CUST" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        private String country = "EE";
        private List<BalanceResponse> balances = Arrays.asList();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public AccountResponseBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public AccountResponseBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public AccountResponseBuilder country(String country) {
            this.country = country;
            return this;
        }

        public AccountResponseBuilder balances(List<BalanceResponse> balances) {
            this.balances = balances;
            return this;
        }

        public AccountResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccountResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AccountResponse build() {
            AccountResponse response = new AccountResponse();
            response.setAccountId(accountId);
            response.setCustomerId(customerId);
            response.setCountry(country);
            response.setBalances(balances);
            response.setCreatedAt(createdAt);
            response.setUpdatedAt(updatedAt);
            return response;
        }
    }

    public static class BalanceResponseBuilder {
        private String balanceId = "BAL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private Currency currency = Currency.EUR;
        private BigDecimal availableAmount = BigDecimal.ZERO;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public BalanceResponseBuilder balanceId(String balanceId) {
            this.balanceId = balanceId;
            return this;
        }

        public BalanceResponseBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public BalanceResponseBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public BalanceResponseBuilder availableAmount(BigDecimal availableAmount) {
            this.availableAmount = availableAmount;
            return this;
        }

        public BalanceResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BalanceResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public BalanceResponse build() {
            BalanceResponse response = new BalanceResponse();
            response.setBalanceId(balanceId);
            response.setAccountId(accountId);
            response.setCurrency(currency);
            response.setAvailableAmount(availableAmount);
            response.setCreatedAt(createdAt);
            response.setUpdatedAt(updatedAt);
            return response;
        }
    }


    public static class TransactionResponseBuilder {
        private String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String accountId = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String balanceId = "BAL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private BigDecimal amount = BigDecimal.valueOf(100.00);
        private Currency currency = Currency.EUR;
        private TransactionDirection direction = TransactionDirection.IN;
        private String description = "Test transaction";
        private BigDecimal balanceAfterTransaction = BigDecimal.valueOf(100.00);
        private String status = "COMPLETED";
        private String idempotencyKey = "idempotency-" + UUID.randomUUID().toString().substring(0, 8);
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private BalanceResponse balance = null;

        public TransactionResponseBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionResponseBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public TransactionResponseBuilder balanceId(String balanceId) {
            this.balanceId = balanceId;
            return this;
        }

        public TransactionResponseBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public TransactionResponseBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public TransactionResponseBuilder direction(TransactionDirection direction) {
            this.direction = direction;
            return this;
        }

        public TransactionResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TransactionResponseBuilder balanceAfterTransaction(BigDecimal balanceAfterTransaction) {
            this.balanceAfterTransaction = balanceAfterTransaction;
            return this;
        }

        public TransactionResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public TransactionResponseBuilder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public TransactionResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransactionResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TransactionResponseBuilder balance(BalanceResponse balance) {
            this.balance = balance;
            return this;
        }

        public TransactionResponse build() {
            TransactionResponse response = new TransactionResponse();
            response.setTransactionId(transactionId);
            response.setAccountId(accountId);
            response.setBalanceId(balanceId);
            response.setAmount(amount);
            response.setCurrency(currency);
            response.setDirection(direction);
            response.setDescription(description);
            response.setBalanceAfterTransaction(balanceAfterTransaction);
            response.setStatus(status);
            response.setIdempotencyKey(idempotencyKey);
            response.setCreatedAt(createdAt);
            response.setUpdatedAt(updatedAt);
            response.setBalance(balance);
            return response;
        }
    }

    public static AccountBuilder account() {
        return new AccountBuilder();
    }

    public static BalanceBuilder balance() {
        return new BalanceBuilder();
    }

    public static TransactionBuilder transaction() {
        return new TransactionBuilder();
    }

    public static CreateAccountRequestBuilder createAccountRequest() {
        return new CreateAccountRequestBuilder();
    }

    public static CreateTransactionRequestBuilder createTransactionRequest() {
        return new CreateTransactionRequestBuilder();
    }

    public static AccountResponseBuilder accountResponse() {
        return new AccountResponseBuilder();
    }

    public static BalanceResponseBuilder balanceResponse() {
        return new BalanceResponseBuilder();
    }

    public static TransactionResponseBuilder transactionResponse() {
        return new TransactionResponseBuilder();
    }
} 