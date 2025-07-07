package com.tuum.acceptancetest.service;

import com.tuum.acceptancetest.config.TestConfig;
import com.tuum.acceptancetest.utils.TestContext;
import com.tuum.acceptancetest.utils.TestDataGenerator;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;


public class ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    

    public Response createAccount(String customerId, String country, String[] currencies) {
        String idempotencyKey = TestDataGenerator.generateIdempotencyKey("account");
        TestContext.setValue("idempotencyKey", idempotencyKey);
        
        // Build JSON array for currencies
        StringBuilder currenciesJson = new StringBuilder();
        currenciesJson.append("[");
        for (int i = 0; i < currencies.length; i++) {
            currenciesJson.append('"').append(currencies[i]).append('"');
            if (i < currencies.length - 1) currenciesJson.append(", ");
        }
        currenciesJson.append("]");

        String requestBody = String.format("{\n  \"customerId\": \"%s\",\n  \"country\": \"%s\",\n  \"currencies\": %s\n}", 
            customerId, country, currenciesJson);

        Allure.addDescription("Creating account with idempotency key: " + idempotencyKey);
        Allure.addAttachment("Account Creation Request", "application/json", requestBody);
        Allure.addAttachment("Request Headers", "text/plain", "Idempotency-Key: " + idempotencyKey);

        Response response = given()
                .spec(TestConfig.getRequestSpec())
                .header("Idempotency-Key", idempotencyKey)
                .body(requestBody)
                .when()
                .post("/accounts")
                .then()
                .extract().response();

        attachResponseToAllure(response, "Account Creation Response");
        TestContext.setValue("accountResponse", response);
        
        logger.info("Account creation request sent with idempotency key: {} and body: {}", idempotencyKey, requestBody);
        return response;
    }


    public Response getAccount(String accountId) {
        Allure.addDescription("Getting account details for account: " + accountId);

        Response response = given()
                .spec(TestConfig.getRequestSpec())
                .when()
                .get("/accounts/{accountId}", accountId)
                .then()
                .extract().response();

        attachResponseToAllure(response, "Account Details Response");
        logger.info("Account details requested for account: {}", accountId);
        return response;
    }

   
    public Response createTransaction(String accountId, double amount, String currency, String direction, String description) {
        String idempotencyKey = TestDataGenerator.generateIdempotencyKey("transaction");
        TestContext.setValue("transactionIdempotencyKey", idempotencyKey);

        String requestBody = String.format("{\n  \"accountId\": \"%s\",\n  \"amount\": %.2f,\n  \"currency\": \"%s\",\n  \"direction\": \"%s\",\n  \"description\": \"%s\"\n}", 
            accountId, amount, currency, direction, description);

        Allure.addDescription("Creating transaction with idempotency key: " + idempotencyKey);
        Allure.addAttachment("Transaction Creation Request", "application/json", requestBody);
        Allure.addAttachment("Request Headers", "text/plain", "Idempotency-Key: " + idempotencyKey);

        Response response = given()
                .spec(TestConfig.getRequestSpec())
                .header("Idempotency-Key", idempotencyKey)
                .body(requestBody)
                .when()
                .post("/transactions")
                .then()
                .extract().response();

        attachResponseToAllure(response, "Transaction Creation Response");
        TestContext.setValue("transactionResponse", response);
        
        logger.info("Transaction creation request sent with idempotency key: {} and body: {}", idempotencyKey, requestBody);
        return response;
    }


    public Response getTransaction(String transactionId) {
        Allure.addDescription("Getting transaction details for transaction: " + transactionId);

        Response response = given()
                .spec(TestConfig.getRequestSpec())
                .when()
                .get("/transactions/{transactionId}", transactionId)
                .then()
                .extract().response();

        attachResponseToAllure(response, "Transaction Details Response");
        logger.info("Transaction details requested for transaction: {}", transactionId);
        return response;
    }


    public Response getAccountTransactions(String accountId) {
        Allure.addDescription("Getting transactions for account: " + accountId);

        Response response = given()
                .spec(TestConfig.getRequestSpec())
                .when()
                .get("/transactions/account/{accountId}", accountId)
                .then()
                .extract().response();

        attachResponseToAllure(response, "Account Transactions Response");
        logger.info("Account transactions requested for account: {}", accountId);
        return response;
    }


    public Response makeRequest(String method, String endpoint, String requestBody, Map<String, String> headers) {
        Allure.addDescription("Making " + method + " request to " + endpoint);
        
        if (requestBody != null) {
            Allure.addAttachment("Request Body", "application/json", requestBody);
        }
        
        if (headers != null && !headers.isEmpty()) {
            StringBuilder headerInfo = new StringBuilder();
            headers.forEach((key, value) -> headerInfo.append(key).append(": ").append(value).append("\n"));
            Allure.addAttachment("Request Headers", "text/plain", headerInfo.toString());
        }

        var requestSpec = given().spec(TestConfig.getRequestSpec());
        
        if (headers != null) {
            headers.forEach(requestSpec::header);
        }
        
        if (requestBody != null) {
            requestSpec.body(requestBody);
        }

        Response response = requestSpec
                .when()
                .request(method, endpoint)
                .then()
                .extract().response();

        attachResponseToAllure(response, method + " " + endpoint + " Response");
        return response;
    }


    public Response createAccountWithBalance(String customerId, String country, String currency, double initialBalance) {
        // First create the account
        Response accountResponse = createAccount(customerId, country, new String[]{currency});
        
        if (accountResponse.getStatusCode() == 201) {
            String accountId = accountResponse.jsonPath().getString("accountId");
            
            // Add initial balance
            Response balanceResponse = createTransaction(accountId, initialBalance, currency, "IN", "Initial balance");
            
            if (balanceResponse.getStatusCode() == 200) {
                Allure.addDescription("✅ Account created with initial balance: " + initialBalance + " " + currency);
            }
        }
        
        return accountResponse;
    }


    public void createMultipleTransactions(String accountId, int count, double baseAmount, String currency) {
        Allure.addDescription("Creating " + count + " transactions for account: " + accountId);
        
        for (int i = 1; i <= count; i++) {
            String idempotencyKey = TestDataGenerator.generateIdempotencyKey("multi-txn-" + i);
            double amount = baseAmount * i;

            String transactionBody = String.format("""
                    {
                        "accountId": "%s",
                        "amount": %.2f,
                        "currency": "%s",
                        "direction": "IN",
                        "description": "Test transaction %d"
                    }
                    """, accountId, amount, currency, i);

            Allure.addAttachment("Transaction " + i + " Request", "application/json", transactionBody);

            Response transactionResponse = given()
                    .spec(TestConfig.getRequestSpec())
                    .header("Idempotency-Key", idempotencyKey)
                    .body(transactionBody)
                    .when()
                    .post("/transactions")
                    .then()
                    .statusCode(201)
                    .extract().response();

            Allure.addAttachment("Transaction " + i + " Response", "application/json", 
                transactionResponse.getBody().asPrettyString());
            Allure.addDescription("✅ Transaction " + i + " created with amount: " + amount);

            logger.info("Created transaction {} with amount: {}", i, amount);
        }
    }


    private void attachResponseToAllure(Response response, String attachmentName) {
        Allure.addAttachment(attachmentName, "application/json", 
            response.getBody().asPrettyString());
        Allure.addAttachment("Response Headers", "text/plain", 
            "Status Code: " + response.getStatusCode() + "\nContent-Type: " + response.getContentType());
    }


    public static ApiService getInstance() {
        return new ApiService();
    }
} 