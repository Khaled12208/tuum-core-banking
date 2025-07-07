package com.tuum.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdGenerator Tests")
class IdGeneratorTest {

    @Test
    @DisplayName("Should generate unique account IDs")
    void shouldGenerateUniqueAccountIds() {
        Set<String> accountIds = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            String accountId = IdGenerator.generateAccountId();
            assertTrue(accountIds.add(accountId), "Account ID should be unique: " + accountId);
            assertTrue(accountId.startsWith("ACC_"), "Account ID should start with ACC_");
        }
    }

    @Test
    @DisplayName("Should generate unique transaction IDs")
    void shouldGenerateUniqueTransactionIds() {
        Set<String> transactionIds = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            String transactionId = IdGenerator.generateTransactionId();
            assertTrue(transactionIds.add(transactionId), "Transaction ID should be unique: " + transactionId);
            assertTrue(transactionId.startsWith("TXN_"), "Transaction ID should start with TXN_");
        }
    }

    @Test
    @DisplayName("Should generate unique balance IDs")
    void shouldGenerateUniqueBalanceIds() {
        Set<String> balanceIds = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            String balanceId = IdGenerator.generateBalanceId();
            assertTrue(balanceIds.add(balanceId), "Balance ID should be unique: " + balanceId);
            assertTrue(balanceId.startsWith("BAL_"), "Balance ID should start with BAL_");
        }
    }

    @Test
    @DisplayName("Should generate unique UUIDs")
    void shouldGenerateUniqueUUIDs() {
        Set<String> uuids = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            String uuid = IdGenerator.generateUUID();
            assertTrue(uuids.add(uuid), "UUID should be unique: " + uuid);
            assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), 
                "UUID should match standard format: " + uuid);
        }
    }

    @Test
    @DisplayName("Should generate IDs with correct format")
    void shouldGenerateIdsWithCorrectFormat() {
        String accountId = IdGenerator.generateAccountId();
        String transactionId = IdGenerator.generateTransactionId();
        String balanceId = IdGenerator.generateBalanceId();
        
        // Check format: PREFIX_TIMESTAMP_RANDOM_UUID
        assertTrue(accountId.matches("ACC_\\d+_\\d+_[a-f0-9]{8}"), 
            "Account ID format should be ACC_TIMESTAMP_RANDOM_UUID: " + accountId);
        assertTrue(transactionId.matches("TXN_\\d+_\\d+_[a-f0-9]{8}"), 
            "Transaction ID format should be TXN_TIMESTAMP_RANDOM_UUID: " + transactionId);
        assertTrue(balanceId.matches("BAL_\\d+_\\d+_[a-f0-9]{8}"), 
            "Balance ID format should be BAL_TIMESTAMP_RANDOM_UUID: " + balanceId);
    }

    @RepeatedTest(10)
    @DisplayName("Should be thread-safe for concurrent access")
    void shouldBeThreadSafeForConcurrentAccess() throws InterruptedException {
        Set<String> accountIds = new HashSet<>();
        Set<String> transactionIds = new HashSet<>();
        Set<String> balanceIds = new HashSet<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // Submit tasks to generate IDs concurrently
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                synchronized (accountIds) {
                    accountIds.add(IdGenerator.generateAccountId());
                }
                synchronized (transactionIds) {
                    transactionIds.add(IdGenerator.generateTransactionId());
                }
                synchronized (balanceIds) {
                    balanceIds.add(IdGenerator.generateBalanceId());
                }
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor should terminate within 10 seconds");
        
        // All IDs should be unique
        assertEquals(100, accountIds.size(), "All account IDs should be unique");
        assertEquals(100, transactionIds.size(), "All transaction IDs should be unique");
        assertEquals(100, balanceIds.size(), "All balance IDs should be unique");
    }

    @Test
    @DisplayName("Should handle high concurrency with CompletableFuture")
    void shouldHandleHighConcurrencyWithCompletableFuture() {
        int numberOfTasks = 1000;
        
        CompletableFuture<String>[] accountFutures = new CompletableFuture[numberOfTasks];
        CompletableFuture<String>[] transactionFutures = new CompletableFuture[numberOfTasks];
        CompletableFuture<String>[] balanceFutures = new CompletableFuture[numberOfTasks];
        
        // Create concurrent tasks
        for (int i = 0; i < numberOfTasks; i++) {
            accountFutures[i] = CompletableFuture.supplyAsync(IdGenerator::generateAccountId);
            transactionFutures[i] = CompletableFuture.supplyAsync(IdGenerator::generateTransactionId);
            balanceFutures[i] = CompletableFuture.supplyAsync(IdGenerator::generateBalanceId);
        }
        
        // Wait for all tasks to complete
        CompletableFuture.allOf(accountFutures).join();
        CompletableFuture.allOf(transactionFutures).join();
        CompletableFuture.allOf(balanceFutures).join();
        
        // Collect results
        Set<String> accountIds = new HashSet<>();
        Set<String> transactionIds = new HashSet<>();
        Set<String> balanceIds = new HashSet<>();
        
        for (int i = 0; i < numberOfTasks; i++) {
            accountIds.add(accountFutures[i].join());
            transactionIds.add(transactionFutures[i].join());
            balanceIds.add(balanceFutures[i].join());
        }
        
        // Verify uniqueness
        assertEquals(numberOfTasks, accountIds.size(), "All account IDs should be unique under high concurrency");
        assertEquals(numberOfTasks, transactionIds.size(), "All transaction IDs should be unique under high concurrency");
        assertEquals(numberOfTasks, balanceIds.size(), "All balance IDs should be unique under high concurrency");
    }

    @Test
    @DisplayName("Should generate IDs with increasing timestamps")
    void shouldGenerateIdsWithIncreasingTimestamps() {
        String id1 = IdGenerator.generateAccountId();
        try {
            Thread.sleep(1); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String id2 = IdGenerator.generateAccountId();
        
        // Extract timestamps from IDs
        long timestamp1 = Long.parseLong(id1.split("_")[1]);
        long timestamp2 = Long.parseLong(id2.split("_")[1]);
        
        assertTrue(timestamp2 >= timestamp1, "Second timestamp should be greater than or equal to first");
    }
} 