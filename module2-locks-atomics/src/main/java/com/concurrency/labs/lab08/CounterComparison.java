package com.concurrency.labs.lab08;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lab 08: LongAdder vs AtomicLong Benchmark
 * 
 * TODO: Understand when to use LongAdder vs AtomicLong.
 * 
 * 📝 NOTE: Both are thread-safe counters, but with different tradeoffs:
 * 
 *   AtomicLong:
 *   - Single value with CAS updates
 *   - Under high contention, many CAS retries → poor performance
 *   - Exact value available anytime
 * 
 *   LongAdder:
 *   - Internally maintains multiple cells (one per CPU core)
 *   - Each thread updates its own cell → no contention!
 *   - sum() aggregates all cells (slightly more expensive)
 * 
 * 💡 THINK: When to use which?
 *   - AtomicLong: Low contention, need exact value frequently
 *   - LongAdder: High contention, need sum less frequently (e.g., metrics)
 */
public class CounterComparison {
    
    /**
     * AtomicLong counter - simple but can be slow under contention.
     * 
     * 📝 NOTE: Every increment is a CAS operation.
     * If CAS fails (another thread modified), retry.
     * Under high contention, many retries → wasted CPU cycles.
     */
    public static class AtomicCounter {
        private final AtomicLong counter = new AtomicLong(0);
        
        public void increment() {
            counter.incrementAndGet();
        }
        
        public long get() {
            return counter.get();
        }
    }
    
    /**
     * LongAdder counter - optimized for high contention.
     * 
     * 📝 NOTE: Internally uses striped cells.
     * Each thread tends to update its own cell → minimal contention!
     * 
     * 💡 THINK: Why is sum() slightly slower?
     *   It must read and add all cells together.
     *   But for counters/metrics, we write often and read rarely.
     */
    public static class AdderCounter {
        private final LongAdder counter = new LongAdder();
        
        public void increment() {
            counter.increment();
        }
        
        public long get() {
            return counter.sum();
        }
    }
    
    /**
     * Simple benchmark to compare performance.
     * 
     * TODO: Run this with different thread counts to see the difference.
     * 
     * ⚠️ AVOID: Using this as a proper benchmark!
     *   For accurate results, use JMH (Java Microbenchmark Harness).
     *   This is just for demonstration.
     */
    public static void main(String[] args) throws InterruptedException {
        int threads = Runtime.getRuntime().availableProcessors() * 2;
        int incrementsPerThread = 1_000_000;
        
        System.out.println("Threads: " + threads);
        System.out.println("Increments per thread: " + incrementsPerThread);
        System.out.println();
        
        // Benchmark AtomicLong
        AtomicCounter atomicCounter = new AtomicCounter();
        long atomicTime = benchmark(atomicCounter::increment, threads, incrementsPerThread);
        System.out.println("AtomicLong: " + atomicTime + " ms, count = " + atomicCounter.get());
        
        // Benchmark LongAdder
        AdderCounter adderCounter = new AdderCounter();
        long adderTime = benchmark(adderCounter::increment, threads, incrementsPerThread);
        System.out.println("LongAdder:  " + adderTime + " ms, count = " + adderCounter.get());
        
        // 💡 THINK: LongAdder should be faster under high contention!
        System.out.println();
        System.out.println("LongAdder is " + (atomicTime / (double) adderTime) + "x faster");
    }
    
    private static long benchmark(Runnable task, int threads, int iterations) 
            throws InterruptedException {
        Thread[] threadArray = new Thread[threads];
        
        for (int i = 0; i < threads; i++) {
            threadArray[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    task.run();
                }
            });
        }
        
        long start = System.currentTimeMillis();
        
        for (Thread t : threadArray) {
            t.start();
        }
        
        for (Thread t : threadArray) {
            t.join();
        }
        
        return System.currentTimeMillis() - start;
    }
}
