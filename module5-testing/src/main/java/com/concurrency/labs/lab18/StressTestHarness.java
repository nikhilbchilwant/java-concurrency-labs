package com.concurrency.labs.lab18;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lab 18: Stress Testing Concurrent Code
 * 
 * TODO: Learn to write stress tests that expose concurrency bugs.
 * 
 * 📝 NOTE: Concurrency bugs are often non-deterministic.
 *   A single test run might pass, but the bug exists!
 *   Stress testing increases the chance of exposing races.
 * 
 * Key techniques:
 *   1. Use many threads (more than CPU cores)
 *   2. Run many iterations
 *   3. Use CountDownLatch to start all threads simultaneously
 *   4. Validate invariants after the test
 * 
 * ⚠️ AVOID: Thread.sleep() in tests!
 *   It makes tests slow and doesn't guarantee the race will occur.
 *   Use proper synchronization points instead.
 */
public class StressTestHarness {
    
    /**
     * TODO: Implement a stress test harness.
     * 
     * @param threads number of concurrent threads
     * @param iterationsPerThread iterations each thread performs
     * @param task the task to stress test
     * @return true if test passed (no exceptions)
     */
    public static boolean stressTest(int threads, int iterationsPerThread, Runnable task) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger(0);
        
        // Submit all tasks
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    // Wait for start signal
                    // 💡 THINK: Why use a start gate?
                    // To ensure all threads start at the same time,
                    // maximizing contention!
                    startGate.await();
                    
                    // Run iterations
                    for (int j = 0; j < iterationsPerThread; j++) {
                        task.run();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    endGate.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startGate.countDown();
        
        // Wait for all threads to complete
        try {
            endGate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            executor.shutdown();
        }
        
        return errors.get() == 0;
    }
    
    /**
     * Example: Stress test an unsafe counter to prove it's broken.
     * 
     * 📝 NOTE: This test SHOULD fail to demonstrate the problem!
     */
    public static void demonstrateRaceCondition() {
        final int[] counter = {0}; // Intentionally not thread-safe
        final int threads = 100;
        final int iterations = 1000;
        
        boolean passed = stressTest(threads, iterations, () -> {
            counter[0]++;
        });
        
        int expected = threads * iterations;
        int actual = counter[0];
        
        System.out.println("Expected: " + expected);
        System.out.println("Actual:   " + actual);
        System.out.println("Lost updates: " + (expected - actual));
        
        // 💡 THINK: Why is actual < expected?
        // Because counter++ is not atomic - race condition!
        if (actual == expected) {
            System.out.println("Test passed (but bug may still exist!)");
        } else {
            System.out.println("Test failed - race condition detected!");
        }
    }
    
    public static void main(String[] args) {
        demonstrateRaceCondition();
    }
}
