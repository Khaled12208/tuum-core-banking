package com.tuum.acceptancetest.steps;

import com.tuum.acceptancetest.service.ApiService;
import com.tuum.acceptancetest.service.VerificationService;
import com.tuum.acceptancetest.utils.TestContext;
import com.tuum.acceptancetest.utils.TestDataGenerator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Transaction Processing Step Definitions
 * Following SOLID principles with generic, reusable components
 */
public class TransactionProcessingSteps extends BaseSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessingSteps.class);
    private final ApiService apiService = ApiService.getInstance();
    private final VerificationService verificationService = VerificationService.getInstance();

    @Before
    public void setup() {
        super.setup();
    }

    @Given("I have an existing account with sufficient balance")
    @Step("Create account with sufficient balance for transactions")
    public void iHaveAnExistingAccountWithSufficientBalance() {
        // Create an account first if not exists
        if (!TestContext.containsKey("accountId")) {
            Allure.addDescription("Creating a new account with sufficient balance for transaction testing");
            
            // Create account with initial balance
            String customerId = TestDataGenerator.generateUniqueCustomerId();
            response = apiService.createAccountWithBalance(customerId, "EE", "EUR", 1000.00);
            verificationService.verifyAccountCreation(response);
            
            String accountId = response.jsonPath().getString("accountId");
            TestContext.setValue("accountId", accountId);
            TestContext.setValue("customerId", customerId);
        }

        String accountId = TestContext.getStringValue("accountId");
        Allure.addDescription("Using account with sufficient balance: " + accountId);
        Allure.addAttachment("Test Account ID", "text/plain", accountId);
        
        logger.info("Using account with sufficient balance: {}", accountId);
    }

    @Given("I want to create a transaction with the following details:")
    @Step("Prepare transaction with data table")
    public void iWantToCreateATransactionWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> transactionData = extractDataFromTable(dataTable);
        
        String accountId = resolveDynamicValue(transactionData.get("accountId"), "accountId");
        Double amount = Double.parseDouble(transactionData.get("amount"));
        String currency = transactionData.get("currency");
        String direction = transactionData.get("direction");
        String description = transactionData.get("description");
        
        TestContext.setValue("accountId", accountId);
        TestContext.setValue("transactionAmount", amount);
        TestContext.setValue("transactionCurrency", currency);
        TestContext.setValue("transactionDirection", direction);
        TestContext.setValue("transactionDescription", description);

        Allure.addDescription("Preparing transaction with data table");
        Allure.addAttachment("Transaction Data", "text/plain", 
            "Account ID: " + accountId + "\nAmount: " + amount + "\nCurrency: " + currency + 
            "\nDirection: " + direction + "\nDescription: " + description);
        
        logger.info("Preparing transaction - Account: {}, Amount: {} {}, Direction: {}, Description: {}", 
                   accountId, amount, currency, direction, description);
    }

    @Given("I want to create a transaction with exact payload:")
    @Step("Prepare transaction with exact payload")
    public void iWantToCreateATransactionWithExactPayload(DataTable dataTable) {
        Map<String, String> transactionData = extractDataFromTable(dataTable);
        
        String accountId = transactionData.get("accountId");
        Double amount = Double.parseDouble(transactionData.get("amount"));
        String currency = transactionData.get("currency");
        String direction = transactionData.get("direction");
        String description = transactionData.get("description");
        
        TestContext.setValue("accountId", accountId);
        TestContext.setValue("transactionAmount", amount);
        TestContext.setValue("transactionCurrency", currency);
        TestContext.setValue("transactionDirection", direction);
        TestContext.setValue("transactionDescription", description);

        Allure.addDescription("Preparing transaction with exact payload");
        Allure.addAttachment("Exact Transaction Data", "text/plain", 
            "Account ID: " + accountId + "\nAmount: " + amount + "\nCurrency: " + currency + 
            "\nDirection: " + direction + "\nDescription: " + description);
        
        logger.info("Preparing transaction with exact payload - Account: {}, Amount: {} {}, Direction: {}, Description: {}", 
                   accountId, amount, currency, direction, description);
    }

    @When("I send a request to create the transaction")
    @Step("Send transaction creation request")
    public void iSendARequestToCreateTheTransaction() {
        String accountId = TestContext.getStringValue("accountId");
        Double amount = (Double) TestContext.getValue("transactionAmount");
        String currency = TestContext.getStringValue("transactionCurrency");
        String direction = TestContext.getStringValue("transactionDirection");
        String description = TestContext.getStringValue("transactionDescription");

        response = apiService.createTransaction(accountId, amount, currency, direction, description);
    }

    @Then("the transaction should be created successfully")
    @Step("Verify transaction creation success")
    public void theTransactionShouldBeCreatedSuccessfully() {
        verificationService.verifyTransactionCreation(response);
    }

    @Then("the response should contain the transaction details")
    @Step("Verify transaction details in response")
    public void theResponseShouldContainTheTransactionDetails() {
        verificationService.verifyTransactionCreation(response);
    }

    @Then("the account balance should be updated correctly")
    @Step("Verify account balance update")
    public void theAccountBalanceShouldBeUpdatedCorrectly() {
        String accountId = TestContext.getStringValue("accountId");
        String currency = TestContext.getStringValue("transactionCurrency");
        Double transactionAmount = (Double) TestContext.getValue("transactionAmount");
        String direction = TestContext.getStringValue("transactionDirection");

        Allure.addDescription("Verifying account balance update after " + direction + " transaction of " + transactionAmount + " " + currency);

        // Wait a moment for the balance to be updated
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Response accountResponse = apiService.getAccount(accountId);
        verificationService.verifyBalanceUpdate(accountResponse, currency, transactionAmount, direction);
    }

    @Given("I have an existing transaction")
    @Step("Create existing transaction for testing")
    public void iHaveAnExistingTransaction() {
        // Create a transaction first if not exists
        if (!TestContext.containsKey("transactionId")) {
            Allure.addDescription("Creating a new transaction for testing purposes");
            
            // Set up transaction data
            TestContext.setValue("transactionAmount", 50.00);
            TestContext.setValue("transactionCurrency", "EUR");
            TestContext.setValue("transactionDirection", "IN");
            TestContext.setValue("transactionDescription", "Test IN transaction");
            
            response = apiService.createTransaction(
                TestContext.getStringValue("accountId"),
                50.00,
                "EUR",
                "IN",
                "Test IN transaction"
            );
            verificationService.verifyTransactionCreation(response);
        }
        
        String transactionId = TestContext.getStringValue("transactionId");
        Allure.addDescription("Using existing transaction: " + transactionId);
        Allure.addAttachment("Test Transaction ID", "text/plain", transactionId);
        
        logger.info("Using existing transaction: {}", transactionId);
    }

    @When("I request the transaction details")
    @Step("Request transaction details")
    public void iRequestTheTransactionDetails() {
        String transactionId = TestContext.getStringValue("transactionId");
        
        // Ensure we have a valid transaction ID
        assertThat(transactionId).isNotNull().isNotEmpty().withFailMessage("Transaction ID is required but was null or empty");

        response = apiService.getTransaction(transactionId);
    }

    @Then("the transaction details should be returned successfully")
    @Step("Verify transaction details retrieval success")
    public void theTransactionDetailsShouldBeReturnedSuccessfully() {
        verificationService.verifyResponseStatus(response, 200, "Transaction Details Retrieval");
    }

    @Then("the response should contain the correct transaction information")
    @Step("Verify correct transaction information")
    public void theResponseShouldContainTheCorrectTransactionInformation() {
        verificationService.verifyTransactionInformation(response);
    }

    @Given("I have an account with multiple transactions")
    @Step("Create account with multiple transactions")
    public void iHaveAnAccountWithMultipleTransactions() {
        // Create an account if not exists
        if (!TestContext.containsKey("accountId")) {
            Allure.addDescription("Creating a new account for multiple transaction testing");
            iHaveAnExistingAccountWithSufficientBalance();
        }

        String accountId = TestContext.getStringValue("accountId");
        Allure.addDescription("Creating multiple transactions for account: " + accountId);

        apiService.createMultipleTransactions(accountId, 3, 10.0, "EUR");
        Allure.addDescription("✅ Created account with multiple transactions: " + accountId);
        logger.info("Created account with multiple transactions: {}", accountId);
    }

    @When("I request the account transactions")
    @Step("Request account transactions")
    public void iRequestTheAccountTransactions() {
        String accountId = TestContext.getStringValue("accountId");
        response = apiService.getAccountTransactions(accountId);
    }

    @Then("the account transactions should be returned successfully")
    @Step("Verify account transactions retrieval success")
    public void theAccountTransactionsShouldBeReturnedSuccessfully() {
        verificationService.verifyResponseStatus(response, 200, "Account Transactions Retrieval");
    }

    @Then("the response should contain a list of transactions")
    @Step("Verify list of transactions in response")
    public void theResponseShouldContainAListOfTransactions() {
        verificationService.verifyTransactionList(response);
    }
} 