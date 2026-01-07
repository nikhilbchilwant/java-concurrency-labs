package com.concurrency.labs.lab13;

import java.util.concurrent.*;

/**
 * Lab 13: CompletableFuture Pipelines
 * 
 * TODO: Build async workflows using CompletableFuture.
 * 
 * 📝 NOTE: CompletableFuture is Java's Promise/Future implementation
 * for composable async programming. Key methods:
 * 
 *   - thenApply: Transform result (like map)
 *   - thenCompose: Chain another async operation (like flatMap)
 *   - thenCombine: Combine two independent futures
 *   - exceptionally: Handle exceptions
 *   - allOf / anyOf: Wait for multiple futures
 * 
 * 💡 THINK: thenApply vs thenCompose:
 *   - thenApply(x -> x.toUpperCase()) - sync transformation
 *   - thenCompose(x -> fetchAsync(x)) - async chain (returns CompletableFuture)
 */
public class CompletableFuturePipeline {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    /**
     * TODO: Implement a simple async pipeline.
     * 
     * Pipeline: fetch user → fetch orders → calculate total
     * 
     * 🔑 HINT:
     *   return fetchUserAsync(userId)
     *       .thenCompose(user -> fetchOrdersAsync(user.getId()))
     *       .thenApply(orders -> calculateTotal(orders));
     */
    public CompletableFuture<Double> getUserOrderTotal(String userId) {
        // TODO: Implement async pipeline
        // Step 1: Fetch user asynchronously
        // Step 2: Use thenCompose to fetch orders (returns another future)
        // Step 3: Use thenApply to calculate total (sync transformation)
        return CompletableFuture.completedFuture(0.0);
    }
    
    /**
     * TODO: Implement parallel async operations with thenCombine.
     * 
     * 💡 THINK: When to use thenCombine vs thenCompose?
     *   - thenCompose: Sequential dependencies (B needs result of A)
     *   - thenCombine: Independent operations run in parallel
     */
    public CompletableFuture<String> fetchUserWithPreferences(String userId) {
        // TODO: Fetch user AND preferences in parallel, then combine
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(
                () -> "User-" + userId, executor);
        CompletableFuture<String> prefsFuture = CompletableFuture.supplyAsync(
                () -> "Prefs-" + userId, executor);
        
        // 🔑 HINT: userFuture.thenCombine(prefsFuture, (user, prefs) -> ...)
        return userFuture.thenCombine(prefsFuture, 
                (user, prefs) -> user + " with " + prefs);
    }
    
    /**
     * TODO: Handle exceptions in async pipeline.
     * 
     * 📝 NOTE: Exception handling methods:
     *   - exceptionally: Recover from exception with default value
     *   - handle: Handle both success and failure cases
     *   - whenComplete: Side effect on completion (doesn't transform)
     * 
     * ⚠️ AVOID: Not handling exceptions at all!
     *   Uncaught exceptions in async code are often silently swallowed.
     */
    public CompletableFuture<String> fetchWithFallback(String id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id.isEmpty()) {
                throw new IllegalArgumentException("ID cannot be empty");
            }
            return "Data-" + id;
        }, executor)
        // TODO: Add exception handling
        // 🔑 HINT: .exceptionally(ex -> "Default value")
        .exceptionally(ex -> {
            System.err.println("Fetch failed: " + ex.getMessage());
            return "Default";
        });
    }
    
    /**
     * TODO: Wait for multiple futures with allOf.
     * 
     * 💡 THINK: allOf returns CompletableFuture<Void>
     *   You need to extract results from the original futures!
     */
    public CompletableFuture<Integer> sumOfMultipleFetches(String... ids) {
        @SuppressWarnings("unchecked")
        CompletableFuture<Integer>[] futures = new CompletableFuture[ids.length];
        
        for (int i = 0; i < ids.length; i++) {
            final int idx = i;
            futures[i] = CompletableFuture.supplyAsync(
                    () -> ids[idx].length(), executor);
        }
        
        // TODO: Wait for all and sum results
        // 🔑 HINT:
        // return CompletableFuture.allOf(futures)
        //     .thenApply(v -> Arrays.stream(futures)
        //         .mapToInt(CompletableFuture::join)
        //         .sum());
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    int sum = 0;
                    for (CompletableFuture<Integer> f : futures) {
                        sum += f.join();
                    }
                    return sum;
                });
    }
    
    /**
     * TODO: Implement timeout for async operations.
     * 
     * 📝 NOTE: In Java 9+, use orTimeout() or completeOnTimeout()
     *   In Java 8, you need to implement manually.
     */
    public CompletableFuture<String> fetchWithTimeout(String id, long timeoutMs) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Data-" + id;
        }, executor);
        
        // Java 9+ version:
        // return future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        
        // 💡 THINK: How would you implement this in Java 8?
        // Hint: Create a second future that completes exceptionally after timeout
        return future;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
