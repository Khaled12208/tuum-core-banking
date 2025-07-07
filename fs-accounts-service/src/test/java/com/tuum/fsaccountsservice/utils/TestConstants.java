package com.tuum.fsaccountsservice.utils;

import com.tuum.common.types.Currency;
import com.tuum.common.types.TransactionDirection;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Test constants following DRY principle and making tests more maintainable.
 * Centralizes common test data values.
 */
public final class TestConstants {

    // Private constructor to prevent instantiation
    private TestConstants() {}

    // Account related constants
    public static final String DEFAULT_ACCOUNT_ID = "ACC12345678";
    public static final String DEFAULT_CUSTOMER_ID = "CUST001";
    public static final String DEFAULT_COUNTRY = "EE";
    public static final String DEFAULT_IDEMPOTENCY_KEY = "test-idempotency-key-123";

    // Balance related constants
    public static final String DEFAULT_BALANCE_ID = "BAL12345678";
    public static final BigDecimal DEFAULT_BALANCE_AMOUNT = BigDecimal.valueOf(1000.00);
    public static final int DEFAULT_VERSION_NUMBER = 1;

    // Transaction related constants
    public static final String DEFAULT_TRANSACTION_ID = "TXN12345678";
    public static final BigDecimal DEFAULT_TRANSACTION_AMOUNT = BigDecimal.valueOf(100.00);
    public static final String DEFAULT_TRANSACTION_DESCRIPTION = "Test transaction";

    // Currency constants
    public static final Currency DEFAULT_CURRENCY = Currency.EUR;
    public static final List<Currency> DEFAULT_CURRENCIES = Arrays.asList(Currency.EUR, Currency.USD);
    public static final List<Currency> MULTI_CURRENCIES = Arrays.asList(Currency.EUR, Currency.USD, Currency.SEK, Currency.GBP);

    // Direction constants
    public static final TransactionDirection DEFAULT_DIRECTION = TransactionDirection.IN;

    // Test data sets for parameterized tests
    public static final String[][] ACCOUNT_TEST_DATA = {
        {"CUST001", "EE", "EUR", "USD"},
        {"CUST002", "SE", "SEK", "GBP"},
        {"CUST003", "GB", "GBP", "EUR"},
        {"CUST004", "FI", "EUR", "USD"},
        {"CUST005", "NO", "EUR", "GBP"}
    };

    public static final String[][] TRANSACTION_TEST_DATA = {
        {"ACC123", "100.00", "EUR", "IN", "Payment for invoice #1234"},
        {"ACC456", "200.00", "SEK", "OUT", "Withdrawal"},
        {"ACC789", "300.00", "GBP", "IN", "Deposit"},
        {"ACC101", "150.50", "USD", "IN", "Salary payment"},
        {"ACC202", "75.25", "EUR", "OUT", "Shopping"}
    };

    public static final String[] ACCOUNT_IDS = {"ACC123", "ACC456", "ACC789", "ACC101", "ACC202"};
    public static final String[] TRANSACTION_IDS = {"TXN123", "TXN456", "TXN789", "TXN101", "TXN202"};
    public static final String[] CUSTOMER_IDS = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};

    // HTTP related constants
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String TEST_IDEMPOTENCY_KEY = "test-key-123";

    // Error messages
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found with id: ";
    public static final String TRANSACTION_NOT_FOUND_MESSAGE = "Transaction not found with id: ";
    public static final String IDEMPOTENCY_KEY_MISSING_MESSAGE = "Idempotency-Key is missing";
} 