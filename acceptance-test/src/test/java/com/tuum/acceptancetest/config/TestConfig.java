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
    
    private static final String BASE_URL = System.getProperty("base.url", System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8083");
    private static final String API_VERSION = "/api/v1";
    
    public static void setupRestAssured() {
        logger.info("DEBUG: System property 'base.url' = {}", System.getProperty("base.url"));
        logger.info("DEBUG: Environment variable 'BASE_URL' = {}", System.getenv("BASE_URL"));
        logger.info("DEBUG: Final BASE_URL = {}", BASE_URL);
        logger.info("DEBUG: API_VERSION = {}", API_VERSION);
        logger.info("DEBUG: Complete URL = {}", BASE_URL + API_VERSION);
        
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = API_VERSION;
        
        RestAssured.filters(new AllureRestAssured());
        
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