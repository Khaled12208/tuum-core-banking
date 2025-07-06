package com.tuum.acceptancetest.utils;

import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private static final Logger logger = LoggerFactory.getLogger(TestContext.class);
    private static final Map<String, Object> context = new HashMap<>();
    
    @Step("Store value in test context: {key} = {value}")
    public static void setValue(String key, Object value) {
        context.put(key, value);
        logger.info("Stored in test context: {} = {}", key, value);
    }
    
    @Step("Get value from test context: {key}")
    public static Object getValue(String key) {
        Object value = context.get(key);
        logger.info("Retrieved from test context: {} = {}", key, value);
        return value;
    }
    
    public static String getStringValue(String key) {
        Object value = getValue(key);
        return value != null ? value.toString() : null;
    }
    
    public static void clear() {
        context.clear();
        logger.info("Test context cleared");
    }
    
    public static boolean containsKey(String key) {
        return context.containsKey(key);
    }
    
    public static void remove(String key) {
        context.remove(key);
        logger.info("Removed from test context: {}", key);
    }
} 