package com.tuum.acceptancetest.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TestDataGenerator {
    
    public static String generateUniqueCustomerId() {
        return "CUST_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static String generateUniqueAccountId() {
        return "ACC_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static String generateUniqueTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static String generateIdempotencyKey(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public static double generateRandomAmount(double min, double max) {
        return Math.round((Math.random() * (max - min) + min) * 100.0) / 100.0;
    }
    
    public static String[] getSupportedCurrencies() {
        return new String[]{"EUR", "SEK", "GBP", "USD"};
    }
    
    public static String getRandomCurrency() {
        String[] currencies = getSupportedCurrencies();
        return currencies[(int) (Math.random() * currencies.length)];
    }
} 