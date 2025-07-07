package com.tuum.acceptancetest.service;

import com.tuum.acceptancetest.utils.TestContext;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class VerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    
 
    public void verifyResponseStatus(Response response, int expectedStatus, String operation) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        
        Allure.addDescription("✅ " + operation + " successful with status code: " + response.getStatusCode());
        Allure.addAttachment(operation + " Verification", "text/plain", 
            "Expected Status: " + expectedStatus + "\nActual Status: " + response.getStatusCode() + "\nResult: PASSED");
        
        logger.info("{} successful with status code: {}", operation, response.getStatusCode());
    }


    public void verifyResponseFields(Response response, Map<String, Object> expectedFields, String operation) {
        StringBuilder verificationDetails = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : expectedFields.entrySet()) {
            String fieldName = entry.getKey();
            Object expectedValue = entry.getValue();
            
            if (expectedValue instanceof String) {
                String actualValue = response.jsonPath().getString(fieldName);
                assertThat(actualValue).isEqualTo(expectedValue);
                verificationDetails.append(fieldName).append(": ").append(actualValue).append(" ✅\n");
            } else if (expectedValue instanceof Double) {
                Double actualValue = response.jsonPath().getDouble(fieldName);
                assertThat(actualValue).isEqualTo(expectedValue);
                verificationDetails.append(fieldName).append(": ").append(actualValue).append(" ✅\n");
            } else if (expectedValue instanceof Integer) {
                Integer actualValue = response.jsonPath().getInt(fieldName);
                assertThat(actualValue).isEqualTo(expectedValue);
                verificationDetails.append(fieldName).append(": ").append(actualValue).append(" ✅\n");
            }
        }
        
        Allure.addDescription("✅ " + operation + " response fields verified successfully");
        Allure.addAttachment(operation + " Field Verification", "text/plain", verificationDetails.toString());
        
        logger.info("{} response fields verified successfully", operation);
    }


    public void verifyAccountCreation(Response response) {
        verifyResponseStatus(response, 201, "Account Creation");
        
        String accountId = response.jsonPath().getString("accountId");
        String customerId = response.jsonPath().getString("customerId");
        String country = response.jsonPath().getString("country");

        assertThat(accountId).isNotNull().isNotEmpty();
        assertThat(customerId).isEqualTo(TestContext.getStringValue("customerId"));
        assertThat(country).isEqualTo(TestContext.getStringValue("country"));

        TestContext.setValue("accountId", accountId);
        
        Allure.addDescription("✅ Account details verified successfully");
        Allure.addAttachment("Account Details Verification", "text/plain", 
            "Account ID: " + accountId + "\nCustomer ID: " + customerId + "\nCountry: " + country);
        
        logger.info("Account details verified - Account ID: {}", accountId);
    }


    public void verifyTransactionCreation(Response response) {
        verifyResponseStatus(response, 201, "Transaction Creation");
        
        String transactionId = response.jsonPath().getString("transactionId");
        String accountId = response.jsonPath().getString("accountId");
        Double amount = response.jsonPath().getDouble("amount");
        String currency = response.jsonPath().getString("currency");
        String direction = response.jsonPath().getString("direction");
        String status = response.jsonPath().getString("status");

        assertThat(transactionId).isNotNull().isNotEmpty();
        assertThat(accountId).isEqualTo(TestContext.getStringValue("accountId"));
        assertThat(amount).isEqualTo(TestContext.getValue("transactionAmount"));
        assertThat(currency).isEqualTo(TestContext.getStringValue("transactionCurrency"));
        assertThat(direction).isEqualTo(TestContext.getStringValue("transactionDirection"));
        
        if (status != null) {
            assertThat(status).isEqualTo("COMPLETED");
        }

        TestContext.setValue("transactionId", transactionId);
        
        Allure.addDescription("✅ Transaction details verified successfully");
        Allure.addAttachment("Transaction Details Verification", "text/plain", 
            "Transaction ID: " + transactionId + "\nAccount ID: " + accountId + 
            "\nAmount: " + amount + "\nCurrency: " + currency + "\nDirection: " + direction + 
            "\nStatus: " + (status != null ? status : "N/A") + "\nResult: PASSED");
        
        logger.info("Transaction details verified - Transaction ID: {}", transactionId);
    }


    public void verifyAccountBalance(Response response, String currency, double expectedAmount) {
        var balances = response.jsonPath().getList("balances");
        assertThat(balances).isNotEmpty();
        
        // Debug: Print all balances returned by the backend
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("DEBUG: All balances returned by backend:\n");
        for (int i = 0; i < balances.size(); i++) {
            String balanceCurrency = response.jsonPath().getString("balances[" + i + "].currency");
            Double amount = response.jsonPath().getDouble("balances[" + i + "].availableAmount");
            debugInfo.append("  Currency: ").append(balanceCurrency).append(", Amount: ").append(amount).append("\n");
        }
        debugInfo.append("Looking for currency: ").append(currency).append(" with expected amount: ").append(expectedAmount);
        
        Allure.addAttachment("Balance Debug Info", "text/plain", debugInfo.toString());
        logger.info(debugInfo.toString());
        
        // Find the balance for the specified currency
        boolean currencyFound = false;
        for (int i = 0; i < balances.size(); i++) {
            String balanceCurrency = response.jsonPath().getString("balances[" + i + "].currency");
            if (currency.equals(balanceCurrency)) {
                Double amount = response.jsonPath().getDouble("balances[" + i + "].availableAmount");
                assertThat(amount).isEqualTo(expectedAmount);
                currencyFound = true;
                
                Allure.addDescription("✅ " + currency + " balance verified - Amount: " + expectedAmount);
                Allure.addAttachment("Balance Verification", "text/plain", 
                    "Currency: " + currency + "\nExpected Amount: " + expectedAmount + "\nActual Amount: " + amount + "\nResult: PASSED");
                
                logger.info("Account balance verified - Currency: {}, Amount: {}", currency, expectedAmount);
                break;
            }
        }
        
        assertThat(currencyFound).isTrue().withFailMessage("Currency %s not found in account balances", currency);
    }


    public void verifyBalanceUpdate(Response response, String currency, double transactionAmount, String direction) {
        var balances = response.jsonPath().getList("balances");
        assertThat(balances).isNotEmpty();
        
        // Find the balance for the specified currency
        Double newBalance = null;
        for (int i = 0; i < balances.size(); i++) {
            String balanceCurrency = response.jsonPath().getString("balances[" + i + "].currency");
            if (currency.equals(balanceCurrency)) {
                newBalance = response.jsonPath().getDouble("balances[" + i + "].availableAmount");
                break;
            }
        }
        
        assertThat(newBalance).isNotNull().isGreaterThanOrEqualTo(0.0);

        if ("IN".equals(direction)) {
            assertThat(newBalance).isGreaterThanOrEqualTo(transactionAmount);
        }

        Allure.addDescription("✅ Account balance updated correctly");
        Allure.addAttachment("Balance Update Verification", "text/plain", 
            "Currency: " + currency + "\nTransaction Amount: " + transactionAmount + 
            "\nDirection: " + direction + "\nNew Balance: " + newBalance + "\nResult: PASSED");

        logger.info("Account balance updated correctly - Currency: {}, New Balance: {}", currency, newBalance);
    }

    /**
     * Verify transaction list
     */
    public void verifyTransactionList(Response response) {
        var transactions = response.jsonPath().getList("$");
        assertThat(transactions).isNotNull();
        assertThat(transactions).hasSizeGreaterThanOrEqualTo(1);
        
        // Verify first transaction has required fields
        String firstTransactionId = response.jsonPath().getString("[0].transactionId");
        String firstAccountId = response.jsonPath().getString("[0].accountId");
        
        assertThat(firstTransactionId).isNotNull().isNotEmpty();
        assertThat(firstAccountId).isEqualTo(TestContext.getStringValue("accountId"));
        
        Allure.addDescription("✅ Transaction list verified successfully");
        Allure.addAttachment("Transaction List Verification", "text/plain", 
            "Total Transactions: " + transactions.size() + "\nFirst Transaction ID: " + firstTransactionId + 
            "\nFirst Account ID: " + firstAccountId + "\nResult: PASSED");
        
        logger.info("Transaction list verified - Found {} transactions", transactions.size());
    }

    public void verifyAllCurrencyBalances(Response response) {
        var balances = response.jsonPath().getList("balances");
        assertThat(balances).isNotNull().isNotEmpty();
        
        StringBuilder balanceDetails = new StringBuilder();
        for (int i = 0; i < balances.size(); i++) {
            String currency = response.jsonPath().getString("balances[" + i + "].currency");
            Double amount = response.jsonPath().getDouble("balances[" + i + "].availableAmount");
            assertThat(currency).isNotNull().isNotEmpty();
            assertThat(amount).isNotNull().isGreaterThanOrEqualTo(0.0);
            
            balanceDetails.append("Currency: ").append(currency)
                         .append(", Amount: ").append(amount)
                         .append(" ✅\n");
            
            logger.info("Balance verified - Currency: {}, Amount: {}", currency, amount);
        }
        
        Allure.addDescription("✅ All currency balances verified successfully");
        Allure.addAttachment("Balance Verification Details", "text/plain", balanceDetails.toString());
    }


    public void verifyAccountInformation(Response response) {
        String accountId = response.jsonPath().getString("accountId");
        String customerId = response.jsonPath().getString("customerId");

        assertThat(accountId).isEqualTo(TestContext.getStringValue("accountId"));
        assertThat(customerId).isNotNull().isNotEmpty(); // Don't check exact match since it's generated
        assertThat(response.jsonPath().getString("country")).isEqualTo("EE");

        Allure.addDescription("✅ Account information verified correctly");
        Allure.addAttachment("Account Information Verification", "text/plain", 
            "Account ID: " + accountId + "\nCustomer ID: " + customerId + "\nCountry: EE\nResult: PASSED");

        logger.info("Account information verified correctly");
    }


    public void verifyTransactionInformation(Response response) {
        String transactionId = response.jsonPath().getString("transactionId");
        String accountId = response.jsonPath().getString("accountId");
        Double amount = response.jsonPath().getDouble("amount");
        String currency = response.jsonPath().getString("currency");
        String direction = response.jsonPath().getString("direction");

        assertThat(transactionId).isEqualTo(TestContext.getStringValue("transactionId"));
        assertThat(accountId).isEqualTo(TestContext.getStringValue("accountId"));
        assertThat(amount).isNotNull().isGreaterThan(0.0);
        assertThat(currency).isNotNull().isNotEmpty();
        assertThat(direction).isIn("IN", "OUT");

        Allure.addDescription("✅ Transaction information verified correctly");
        Allure.addAttachment("Transaction Information Verification", "text/plain", 
            "Transaction ID: " + transactionId + "\nAccount ID: " + accountId + 
            "\nAmount: " + amount + "\nCurrency: " + currency + "\nDirection: " + direction + "\nResult: PASSED");

        logger.info("Transaction information verified correctly");
    }


    public static VerificationService getInstance() {
        return new VerificationService();
    }
} 