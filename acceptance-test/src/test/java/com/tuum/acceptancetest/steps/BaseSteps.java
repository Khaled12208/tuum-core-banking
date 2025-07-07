package com.tuum.acceptancetest.steps;

import com.tuum.acceptancetest.config.TestConfig;
import com.tuum.acceptancetest.utils.TestContext;
import com.tuum.acceptancetest.utils.TestDataGenerator;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


public abstract class BaseSteps {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseSteps.class);
    protected Response response;

    protected void setup() {
        TestConfig.setupRestAssured();
    }

    protected void verifySystemHealth() {
        Allure.addDescription("Checking if the banking system is healthy and ready for testing");
        
        String endpoint = "/actuator/health";
        String fullUrl = TestConfig.getBaseUrl() + endpoint;
        logger.info(" FULL ENDPOINT URL: {}", fullUrl);
        logger.info(" Base URL from config: {}", TestConfig.getBaseUrl());
        logger.info(" Endpoint path: {}", endpoint);
        logger.info(" About to make GET request...");
        
        Response healthResponse = given()
                .spec(TestConfig.getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .extract().response();

        Allure.addAttachment("Health Check Response", "application/json", 
            healthResponse.getBody().asPrettyString());

        assertThat(healthResponse.getStatusCode()).isEqualTo(200);
        logger.info("Banking system health check passed");
        Allure.addDescription("✅ Banking system is healthy and ready for testing");
    }


    protected void generateUniqueCustomerId() {
        String customerId = TestDataGenerator.generateUniqueCustomerId();
        TestContext.setValue("customerId", customerId);
        
        Allure.addDescription("Generated unique customer ID: " + customerId);
        Allure.addAttachment("Generated Customer ID", "text/plain", customerId);
        
        logger.info("Generated customer ID: {}", customerId);
    }


    protected Map<String, String> extractDataFromTable(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        return rows.get(0);
    }


    protected String resolveDynamicValue(String value, String contextKey) {
        if (value != null && value.startsWith("<") && value.endsWith(">")) {
            return TestContext.getStringValue(contextKey);
        }
        return value;
    }


    protected String[] parseCommaSeparatedValues(String values) {
        if (values == null || values.trim().isEmpty()) {
            return new String[0];
        }
        String[] array = values.split(",");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

  
    protected Response makeHttpRequest(String method, String endpoint, String requestBody, Map<String, String> headers) {
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

        // Attach response to Allure
        Allure.addAttachment("Response Body", "application/json", 
            response.getBody().asPrettyString());
        Allure.addAttachment("Response Headers", "text/plain", 
            "Status Code: " + response.getStatusCode() + "\nContent-Type: " + response.getContentType());

        return response;
    }


    protected void verifyResponseStatus(Response response, int expectedStatus, String operation) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        
        Allure.addDescription("✅ " + operation + " successful with status code: " + response.getStatusCode());
        Allure.addAttachment(operation + " Verification", "text/plain", 
            "Expected Status: " + expectedStatus + "\nActual Status: " + response.getStatusCode() + "\nResult: PASSED");
        
        logger.info("{} successful with status code: {}", operation, response.getStatusCode());
    }


    protected void verifyResponseFields(Response response, Map<String, Object> expectedFields, String operation) {
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
            }
        }
        
        Allure.addDescription(" " + operation + " response fields verified successfully");
        Allure.addAttachment(operation + " Field Verification", "text/plain", verificationDetails.toString());
        
        logger.info("{} response fields verified successfully", operation);
    }


    protected String createIdempotencyKey(String prefix) {
        return TestDataGenerator.generateIdempotencyKey(prefix);
    }


    protected String formatJsonBody(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{\n");
        int count = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            json.append("  \"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            if (++count < data.size()) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("}");
        return json.toString();
    }
} 