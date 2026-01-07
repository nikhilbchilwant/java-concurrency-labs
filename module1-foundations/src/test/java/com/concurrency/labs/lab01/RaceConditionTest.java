package com.concurrency.labs.lab01;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Lab 01: Race Condition
 * 
 * 📝 NOTE: These tests demonstrate how to expose race conditions.
 * The key is to use many threads and many iterations to maximize
 * the chance of seeing the bug.
 */
class RaceConditionTest {
    
    /**
     * This test SHOULD fail when run against UnsafeCounter,
     * demonstrating the race condition.
     */
    @RepeatedTest(5)
    void testUnsafeCounter_shouldLoseUpdates() throws InterruptedException {
        UnsafeCounter counter = new UnsafeCounter();
        int threads = 100;
        int incrementsPerThread = 1000;
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for signal to start
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // Start all threads
        endLatch.await(); // Wait for all threads
        executor.shutdown();
        
        int expected = threads * incrementsPerThread;
        int actual = counter.getCount();
        
        // 💡 THINK: This assertion might pass sometimes!
        // Race conditions are non-deterministic.
        // But with 100 threads and 1000 iterations, it usually fails.
        System.out.println("Expected: " + expected + ", Actual: " + actual + 
                           ", Lost: " + (expected - actual));
        
        // We EXPECT this to be less than expected due to race condition
        // (If it equals expected, the race didn't manifest this run)
    }
    
    /**
     * TODO: After implementing SynchronizedCounter, this test should ALWAYS pass.
     */
    @RepeatedTest(5)
    void testSynchronizedCounter_shouldNotLoseUpdates() throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        int threads = 100;
        int incrementsPerThread = 1000;
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();
        
        int expected = threads * incrementsPerThread;
        int actual = counter.getCount();
        
        // TODO: Once you implement SynchronizedCounter correctly,
        // this assertion should ALWAYS pass
        // assertEquals(expected, actual, "No updates should be lost with proper synchronization");
    }
}
