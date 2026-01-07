package com.concurrency.labs.lab20;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lab 20: Deterministic Testing for Concurrent Code
 * 
 * TODO: Learn patterns for making concurrent tests reliable.
 * 
 * ⚠️ AVOID: Flaky tests!
 *   Concurrency tests often fail non-deterministically.
 *   This is unacceptable in CI/CD pipelines.
 * 
 * 📝 NOTE: Key patterns:
 *   1. Control thread execution order with latches
 *   2. Use Awaitility for async assertions
 *   3. Never use Thread.sleep() for synchronization!
 */
public class DeterministicTestingPatterns {
    
    /**
     * ⚠️ BAD: Using Thread.sleep() for synchronization.
     * 
     * Why it's bad:
     *   - Arbitrary timing (might be too short or too long)
     *   - Makes tests slow (always waits full duration)
     *   - Fails under load or on slow machines
     */
    public static void badPattern_sleep() throws InterruptedException {
        AtomicBoolean done = new AtomicBoolean(false);
        
        new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate work
                done.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // ⚠️ BAD: What if work takes longer than 200ms?
        Thread.sleep(200);
        assert done.get() : "Should be done";
        
        // What if work is done in 10ms? We still wait 200ms!
    }
    
    /**
     * ✅ GOOD: Using CountDownLatch for ordering.
     * 
     * 🔑 HINT: Latches let you control exactly when threads proceed.
     */
    public static void goodPattern_latch() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);
        
        Thread worker = new Thread(() -> {
            try {
                ready.await(); // Wait for signal to start
                result.set(true);
                done.countDown(); // Signal completion
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.start();
        
        // Start the worker
        ready.countDown();
        
        // Wait for completion (with timeout!)
        boolean finished = done.await(5, TimeUnit.SECONDS);
        assert finished : "Worker should complete";
        assert result.get() : "Result should be set";
    }
    
    /**
     * ✅ GOOD: Using Awaitility pattern (conceptual).
     * 
     * 📝 NOTE: Awaitility is a library for async assertions.
     * It polls a condition until true (or timeout).
     * 
     * Real Awaitility code:
     *   await().atMost(5, SECONDS).until(() -> cache.size() > 0);
     */
    public static void awaitilityPattern() throws InterruptedException {
        AtomicBoolean condition = new AtomicBoolean(false);
        
        // Start async operation
        new Thread(() -> {
            try {
                Thread.sleep(50);
                condition.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Poll until condition is true (simulating Awaitility)
        long deadline = System.currentTimeMillis() + 5000; // 5 second timeout
        while (!condition.get()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("Condition not met within timeout");
            }
            Thread.sleep(10); // Poll interval
        }
        
        System.out.println("Condition met!");
    }
    
    /**
     * ✅ GOOD: Controlling interleaving with phaser.
     * 
     * 💡 THINK: Sometimes you need specific thread orderings.
     *   Phaser lets you create "phases" that threads sync on.
     */
    public static void controlledInterleaving() throws InterruptedException {
        // Force specific ordering: setup -> modify -> verify
        Phaser phaser = new Phaser(2); // 2 parties
        
        StringBuilder log = new StringBuilder();
        
        Thread modifier = new Thread(() -> {
            phaser.arriveAndAwaitAdvance(); // Wait for phase 1
            log.append("modified");
            phaser.arriveAndAwaitAdvance(); // Wait for phase 2
        });
        
        Thread verifier = new Thread(() -> {
            // Phase 1: modifier runs first
            phaser.arriveAndAwaitAdvance();
            // Phase 2: now we verify
            phaser.arriveAndAwaitAdvance();
            assert log.toString().contains("modified");
        });
        
        modifier.start();
        verifier.start();
        
        modifier.join();
        verifier.join();
        
        System.out.println("Controlled interleaving succeeded");
    }
    
    /**
     * Pattern for testing with timeouts.
     * 
     * 📝 NOTE: Always use timeouts in concurrent tests!
     * A bug might cause infinite blocking.
     */
    public static void timeoutPattern() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Future<String> future = executor.submit(() -> {
            // Simulate work
            Thread.sleep(100);
            return "result";
        });
        
        try {
            // ✅ Always use timeout!
            String result = future.get(5, TimeUnit.SECONDS);
            assert "result".equals(result);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new AssertionError("Test timed out", e);
        } catch (Exception e) {
            throw new AssertionError("Test failed", e);
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * Tips for reliable concurrent tests.
     */
    public static void testingTips() {
        System.out.println("Concurrent Testing Tips:");
        System.out.println();
        System.out.println("1. NEVER use Thread.sleep() for synchronization");
        System.out.println("2. ALWAYS use timeouts (tests should never hang)");
        System.out.println("3. Use CountDownLatch/Phaser to control ordering");
        System.out.println("4. Use Awaitility for polling conditions");
        System.out.println("5. Run flaky tests many times to expose issues");
        System.out.println("6. Consider using JCStress for low-level races");
    }
    
    public static void main(String[] args) throws InterruptedException {
        testingTips();
        System.out.println("\n=== Running patterns ===\n");
        
        System.out.println("Latch pattern:");
        goodPattern_latch();
        
        System.out.println("\nAwaitility pattern:");
        awaitilityPattern();
        
        System.out.println("\nControlled interleaving:");
        controlledInterleaving();
        
        System.out.println("\nTimeout pattern:");
        timeoutPattern();
        
        System.out.println("\nAll patterns succeeded!");
    }
}
