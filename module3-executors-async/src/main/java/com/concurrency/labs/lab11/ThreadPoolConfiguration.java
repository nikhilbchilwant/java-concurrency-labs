package com.concurrency.labs.lab11;

import java.util.concurrent.*;

/**
 * Lab 11: Custom ThreadPoolExecutor Configuration
 * 
 * TODO: Learn to configure ThreadPoolExecutor for different scenarios.
 * 
 * 📝 NOTE: ThreadPoolExecutor constructor parameters:
 *   - corePoolSize: Threads kept alive even when idle
 *   - maxPoolSize: Maximum threads ever created
 *   - keepAliveTime: How long excess threads wait before terminating
 *   - workQueue: Queue for holding tasks before execution
 *   - rejectionPolicy: What to do when queue is full
 * 
 * 💡 THINK: How do corePoolSize, maxPoolSize, and queue interact?
 *   1. New task arrives
 *   2. If threads < corePoolSize → create new thread
 *   3. If threads >= corePoolSize → add to queue
 *   4. If queue is full AND threads < maxPoolSize → create new thread
 *   5. If queue is full AND threads >= maxPoolSize → reject!
 * 
 * ⚠️ AVOID: Unbounded queue with fixed pool!
 *   new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>())
 *   Problem: Queue grows forever if tasks arrive faster than completion → OOM!
 */
public class ThreadPoolConfiguration {
    
    /**
     * TODO: Create a pool for CPU-bound tasks.
     * 
     * 🔑 HINT: For CPU-bound work:
     *   - corePoolSize = maxPoolSize = number of CPU cores
     *   - Small or no queue (you want threads, not queuing)
     * 
     * 💡 THINK: Why not more threads than cores for CPU-bound work?
     *   Context switching overhead - threads compete for limited CPU time.
     */
    public static ExecutorService createCpuBoundPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        
        // TODO: Create ThreadPoolExecutor optimized for CPU-bound work
        // 📝 NOTE: Use SynchronousQueue for immediate handoff (no queuing)
        return new ThreadPoolExecutor(
                cores,                      // corePoolSize
                cores,                      // maxPoolSize (same as core)
                0L, TimeUnit.MILLISECONDS,  // No timeout for core threads
                new SynchronousQueue<>(),   // No queuing - immediate handoff
                new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure
        );
    }
    
    /**
     * TODO: Create a pool for IO-bound tasks.
     * 
     * 🔑 HINT: For IO-bound work:
     *   - More threads than cores (threads often blocked on IO)
     *   - Rule of thumb: cores * 2, or calculate based on wait time
     * 
     * 💡 THINK: Why more threads for IO-bound work?
     *   While one thread waits for IO, others can use the CPU!
     *   Formula: threads = cores * (1 + waitTime/computeTime)
     */
    public static ExecutorService createIoBoundPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        
        // TODO: Create ThreadPoolExecutor optimized for IO-bound work
        return new ThreadPoolExecutor(
                cores,                      // corePoolSize
                cores * 2,                  // maxPoolSize (2x cores)
                60L, TimeUnit.SECONDS,      // Excess threads wait 60s
                new LinkedBlockingQueue<>(100), // Bounded queue!
                new ThreadPoolExecutor.AbortPolicy() // Reject if full
        );
    }
    
    /**
     * TODO: Create a pool with backpressure.
     * 
     * 📝 NOTE: Rejection policies:
     *   - AbortPolicy: Throws RejectedExecutionException
     *   - CallerRunsPolicy: Caller's thread runs the task (backpressure!)
     *   - DiscardPolicy: Silently drops the task
     *   - DiscardOldestPolicy: Drops oldest queued task
     * 
     * 💡 THINK: Which policy is best for your use case?
     *   - CallerRunsPolicy slows down the producer naturally
     *   - AbortPolicy lets you handle rejection explicitly
     */
    public static ExecutorService createPoolWithBackpressure() {
        int cores = Runtime.getRuntime().availableProcessors();
        
        // TODO: Create pool that applies backpressure when overloaded
        // ⚠️ AVOID: DiscardPolicy in production - you lose tasks silently!
        return new ThreadPoolExecutor(
                cores,
                cores,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(10),  // Small bounded queue
                new ThreadPoolExecutor.CallerRunsPolicy() // Caller runs if full!
        );
    }
}
