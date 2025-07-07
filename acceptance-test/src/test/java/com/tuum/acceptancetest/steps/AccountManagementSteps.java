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

/**
 * Account Management Step Definitions
 * Following SOLID principles with generic, reusable components
 */
public class AccountManagementSteps extends BaseSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountManagementSteps.class);
    private final ApiService apiService = ApiService.getInstance();
    private final VerificationService verificationService = VerificationService.getInstance();

    @Before
    public void setup() {
        super.setup();
    }

    @Given("the banking system is running")
    @Step("Verify banking system is running")
    public void theBankingSystemIsRunning() {
        verifySystemHealth();
    }

    @Given("I have a unique customer ID")
    @Step("Generate unique customer ID")
    public void iHaveAUniqueCustomerId() {
        generateUniqueCustomerId();
    }

    @Given("I want to create a new account with the following details:")
    @Step("Prepare account creation with data table")
    public void iWantToCreateANewAccountWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> accountData = extractDataFromTable(dataTable);
        
        String customerId = resolveDynamicValue(accountData.get("customerId"), "customerId");
        String country = accountData.get("country");
        String currencies = accountData.get("currencies");
        
        TestContext.setValue("customerId", customerId);
        TestContext.setValue("country", country);
        TestContext.setValue("currencies", parseCommaSeparatedValues(currencies));
        
        Allure.addDescription("Preparing to create account with data table");
        Allure.addAttachment("Account Creation Data", "text/plain", 
            "Customer ID: " + customerId + "\nCountry: " + country + "\nCurrencies: " + currencies);
        
        logger.info("Preparing to create account - Customer: {}, Country: {}, Currencies: {}", 
                   customerId, country, currencies);
    }

    @Given("I want to create a new account with exact payload:")
    @Step("Prepare account creation with exact payload")
    public void iWantToCreateANewAccountWithExactPayload(DataTable dataTable) {
        Map<String, String> accountData = extractDataFromTable(dataTable);
        
        String customerId = accountData.get("customerId");
        String country = accountData.get("country");
        String currencies = accountData.get("currencies");
        
        TestContext.setValue("customerId", customerId);
        TestContext.setValue("country", country);
        TestContext.setValue("currencies", parseCommaSeparatedValues(currencies));
        
        Allure.addDescription("Preparing to create account with exact payload");
        Allure.addAttachment("Exact Account Creation Data", "text/plain", 
            "Customer ID: " + customerId + "\nCountry: " + country + "\nCurrencies: " + currencies);
        
        logger.info("Preparing to create account with exact payload - Customer: {}, Country: {}, Currencies: {}", 
                   customerId, country, currencies);
    }

    @When("I send a request to create the account")
    @Step("Send account creation request")
    public void iSendARequestToCreateTheAccount() {
        String customerId = TestContext.getStringValue("customerId");
        String country = TestContext.getStringValue("country");
        String[] currencies = (String[]) TestContext.getValue("currencies");
        
        response = apiService.createAccount(customerId, country, currencies);
    }

    @Then("the account should be created successfully")
    @Step("Verify account creation success")
    public void theAccountShouldBeCreatedSuccessfully() {
        verificationService.verifyAccountCreation(response);
    }

    @Then("the response should contain the account details")
    @Step("Verify account details in response")
    public void theResponseShouldContainTheAccountDetails() {
        verificationService.verifyAccountCreation(response);
    }

    @Then("the account should have the following balances initialized:")
    @Step("Verify account balance initialization with data table")
    public void theAccountShouldHaveTheFollowingBalancesInitialized(DataTable dataTable) {
        String accountId = TestContext.getStringValue("accountId");
        Map<String, String> balanceData = extractDataFromTable(dataTable);

        String currency = balanceData.get("currency");
        Double expectedAmount = Double.parseDouble(balanceData.get("expectedAmount"));

        Response accountResponse = apiService.getAccount(accountId);

        if ("ALL_CURRENCIES".equals(currency)) {
            String[] currencies = (String[]) TestContext.getValue("currencies");
            for (String curr : currencies) {
                verificationService.verifyAccountBalance(accountResponse, curr, expectedAmount);
            }
        } else {
            verificationService.verifyAccountBalance(accountResponse, currency, expectedAmount);
        }
    }

    @Given("I have an existing account")
    @Step("Create existing account for testing")
    public void iHaveAnExistingAccount() {
        // Create an account first if not exists
        if (!TestContext.containsKey("accountId")) {
            Allure.addDescription("Creating a new account for testing purposes");
            
            // Set up account creation data
            TestContext.setValue("customerId", TestDataGenerator.generateUniqueCustomerId());
            TestContext.setValue("country", "EE");
            TestContext.setValue("currencies", new String[]{"EUR"});
            
            response = apiService.createAccount(
                TestContext.getStringValue("customerId"),
                TestContext.getStringValue("country"),
                (String[]) TestContext.getValue("currencies")
            );
            verificationService.verifyAccountCreation(response);
        }
        
        String accountId = TestContext.getStringValue("accountId");
        Allure.addDescription("Using existing account: " + accountId);
        Allure.addAttachment("Test Account ID", "text/plain", accountId);
        
        logger.info("Using existing account: {}", accountId);
    }

    @When("I request the account details")
    @Step("Request account details")
    public void iRequestTheAccountDetails() {
        String accountId = TestContext.getStringValue("accountId");
        response = apiService.getAccount(accountId);
    }

    @Then("the account details should be returned successfully")
    @Step("Verify account details retrieval success")
    public void theAccountDetailsShouldBeReturnedSuccessfully() {
        verificationService.verifyResponseStatus(response, 200, "Account Details Retrieval");
    }

    @Then("the response should contain the correct account information")
    @Step("Verify correct account information")
    public void theResponseShouldContainTheCorrectAccountInformation() {
        verificationService.verifyAccountInformation(response);
    }

    @Given("I have an existing account with multiple currencies")
    @Step("Create account with multiple currencies")
    public void iHaveAnExistingAccountWithMultipleCurrencies() {
        // Create account with multiple currencies
        String customerId = TestDataGenerator.generateUniqueCustomerId();
        TestContext.setValue("customerId", customerId); // Set before creation
        String idempotencyKey = TestDataGenerator.generateIdempotencyKey("multi-currency-account");

        String requestBody = """
                {
                    "customerId": "%s",
                    "country": "EE",
                    "currencies": ["EUR", "USD", "SEK"]
                }
                """.formatted(customerId);

        Allure.addDescription("Creating account with multiple currencies: EUR, USD, SEK");
        Allure.addAttachment("Multi-Currency Account Request", "application/json", requestBody);

        response = apiService.createAccount(customerId, "EE", new String[]{"EUR", "USD", "SEK"});
        verificationService.verifyAccountCreation(response);

        String accountId = response.jsonPath().getString("accountId");
        TestContext.setValue("accountId", accountId);

        Allure.addDescription(" Multi-currency account created: " + accountId);
        logger.info("Created multi-currency account: {}", accountId);
    }

    @When("I request the account balances")
    @Step("Request account balances")
    public void iRequestTheAccountBalances() {
        String accountId = TestContext.getStringValue("accountId");
        Allure.addDescription("Requesting account details (includes balances) for account: " + accountId);
        response = apiService.getAccount(accountId);
    }

    @Then("the account balances should be returned successfully")
    @Step("Verify account balances retrieval success")
    public void theAccountBalancesShouldBeReturnedSuccessfully() {
        verificationService.verifyResponseStatus(response, 200, "Account Details Retrieval (includes balances)");
    }

    @Then("each currency should have a balance amount")
    @Step("Verify balance amounts for each currency")
    public void eachCurrencyShouldHaveABalanceAmount() {
        verificationService.verifyAllCurrencyBalances(response);
    }
} 