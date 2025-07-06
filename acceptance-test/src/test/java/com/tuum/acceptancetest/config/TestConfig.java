package com.tuum.acceptancetest.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);
    
    private static final String BASE_URL = System.getProperty("base.url", "http://localhost:8083");
    private static final String API_VERSION = "/api/v1";
    
    public static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = API_VERSION;
        
        // Add Allure filter for reporting
        RestAssured.filters(new AllureRestAssured());
        
        // Add logging filters
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        
        logger.info("RestAssured configured with base URL: {}", BASE_URL + API_VERSION);
    }
    
    public static RequestSpecification getRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath(API_VERSION)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }
    
    public static ResponseSpecification getResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }
    
    public static String getBaseUrl() {
        return BASE_URL + API_VERSION;
    }
} 