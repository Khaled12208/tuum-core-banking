package com.tuum.common.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class IdGenerator {
    
    private static final String ACCOUNT_PREFIX = "ACC";
    private static final String TRANSACTION_PREFIX = "TXN";
    private static final String BALANCE_PREFIX = "BAL";
    

    public static String generateAccountId() {
        return generateId(ACCOUNT_PREFIX);
    }
    

    public static String generateTransactionId() {
        return generateId(TRANSACTION_PREFIX);
    }
    

    public static String generateBalanceId() {
        return generateId(BALANCE_PREFIX);
    }
    

    private static String generateId(String prefix) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long timestamp = System.currentTimeMillis();
        long randomComponent = random.nextLong();
        String uuidComponent = UUID.randomUUID().toString().substring(0, 8);
                return String.format("%s_%d_%d_%s", 
            prefix, 
            timestamp, 
            Math.abs(randomComponent), 
            uuidComponent);
    }
    
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
} 