package com.concurrency.labs.lab12;

import java.util.concurrent.*;

/**
 * Lab 12: Task Cancellation and Timeouts
 * 
 * TODO: Learn proper patterns for cancellation and timeouts.
 * 
 * 📝 NOTE: Cancellation in Java is COOPERATIVE, not preemptive!
 *   You cannot forcibly kill a thread. You must:
 *   1. Set a flag (interrupt)
 *   2. Task must CHECK the flag and respond
 * 
 * 💡 THINK: Why cooperative cancellation?
 *   - Preemptive interruption can leave shared state inconsistent
 *   - Only the task knows safe points to stop
 */
public class CancellationPatterns {
    
    /**
     * TODO: Handle interruption correctly in a loop.
     * 
     * 📝 NOTE: Two types of interruption responses:
     *   1. InterruptedException: Thrown by blocking operations (sleep, wait, take)
     *   2. Thread.interrupted(): Check flag after CPU-bound work
     */
    public static void interruptibleTask() {
        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Blocking operation - can throw InterruptedException
                    Thread.sleep(1000);
                    System.out.println("Working...");
                } catch (InterruptedException e) {
                    // ⚠️ CRITICAL: Restore interrupt flag and exit!
                    // Thread.sleep() CLEARS the interrupt flag when throwing
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted, cleaning up...");
                    break;
                }
            }
            System.out.println("Task completed or cancelled");
        };
        
        Thread worker = new Thread(task);
        worker.start();
        
        // Cancel after 3 seconds
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        worker.interrupt();
    }
    
    /**
     * TODO: Implement timeout using Future.get(timeout).
     * 
     * 🔑 HINT: Future.get(timeout, unit) throws TimeoutException if not done.
     * 
     * ⚠️ AVOID: Forgetting to cancel the task after timeout!
     *   The task keeps running in the background!
     */
    public static <T> T executeWithTimeout(Callable<T> task, long timeout, TimeUnit unit) 
            throws TimeoutException, ExecutionException, InterruptedException {
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(task);
        
        try {
            // Wait with timeout
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            // ⚠️ CRITICAL: Cancel the task!
            // true = may interrupt if running
            future.cancel(true);
            throw e;
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * TODO: Implement cancellable task that checks interrupt flag.
     * 
     * 📝 NOTE: For CPU-bound work, you must periodically check the flag!
     */
    public static long cancellableCpuBoundTask() throws InterruptedException {
        long sum = 0;
        for (int i = 0; i < 1_000_000_000; i++) {
            sum += i;
            
            // 🔑 HINT: Check interrupt flag periodically
            // (Not every iteration - that's expensive!)
            if (i % 1_000_000 == 0 && Thread.currentThread().isInterrupted()) {
                // 💡 THINK: Should we throw or just return?
                // Depends on your contract. Throwing is cleaner.
                throw new InterruptedException("Task cancelled");
            }
        }
        return sum;
    }
    
    /**
     * TODO: Implement shutdown with timeout for ExecutorService.
     * 
     * 📝 NOTE: shutdown() vs shutdownNow():
     *   - shutdown(): Stop accepting new tasks, run queued tasks
     *   - shutdownNow(): Interrupt running tasks, return queued tasks
     */
    public static void shutdownWithTimeout(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown(); // Stop accepting new tasks
        
        try {
            // Wait for completion
            if (!executor.awaitTermination(timeout, unit)) {
                // Timeout - force shutdown
                System.out.println("Timeout, forcing shutdown...");
                executor.shutdownNow();
                
                // Wait again after forcing
                if (!executor.awaitTermination(timeout, unit)) {
                    System.err.println("Executor did not terminate!");
                }
            }
        } catch (InterruptedException e) {
            // Interrupted while waiting
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Demonstrating cancellation ===");
        interruptibleTask();
    }
}
