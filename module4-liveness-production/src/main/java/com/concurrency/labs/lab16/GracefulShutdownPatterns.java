package com.concurrency.labs.lab16;

import java.util.concurrent.*;

/**
 * Lab 16: Graceful Shutdown Patterns
 * 
 * TODO: Learn to shutdown thread pools cleanly.
 * 
 * 📝 NOTE: Graceful shutdown means:
 *   1. Stop accepting new tasks
 *   2. Complete already-submitted tasks
 *   3. Release resources cleanly
 * 
 * ⚠️ AVOID: Abandoning tasks or leaking threads!
 *   - Always call shutdown() when done
 *   - Use try-finally to ensure cleanup
 */
public class GracefulShutdownPatterns {
    
    /**
     * TODO: The standard shutdown pattern.
     * 
     * 🔑 HINT: This is the recommended pattern from Java docs.
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Stop accepting new tasks
        
        try {
            // Wait for existing tasks to complete
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel running tasks
                
                // Wait for tasks to respond to cancellation
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            // Current thread was interrupted while waiting
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * TODO: Poison pill pattern for producer-consumer.
     * 
     * 📝 NOTE: A "poison pill" is a special message that tells
     * consumers to stop processing and shutdown.
     */
    public static void poisonPillExample() throws InterruptedException {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        String POISON_PILL = "DONE";
        
        // Consumer
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String item = queue.take();
                    if (POISON_PILL.equals(item)) {
                        System.out.println("Consumer received poison pill, shutting down");
                        break;
                    }
                    System.out.println("Processing: " + item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();
        
        // Producer
        for (int i = 0; i < 5; i++) {
            queue.put("Item-" + i);
        }
        
        // Send poison pill to shutdown consumer
        queue.put(POISON_PILL);
        
        consumer.join();
        System.out.println("Shutdown complete");
    }
    
    /**
     * TODO: Graceful shutdown with pending task preservation.
     * 
     * 💡 THINK: What if you need to save unfinished tasks?
     *   shutdownNow() returns the list of tasks that never started.
     */
    public static void shutdownPreservingTasks(ExecutorService pool) {
        // Get tasks that haven't started
        java.util.List<Runnable> notStarted = pool.shutdownNow();
        
        System.out.println("Tasks not started: " + notStarted.size());
        
        // You could persist these for later execution
        for (Runnable task : notStarted) {
            // saveForLater(task);
            System.out.println("Saved task: " + task);
        }
    }
    
    /**
     * TODO: Using try-with-resources for ExecutorService (Java 19+).
     * 
     * 📝 NOTE: In Java 19+, ExecutorService implements AutoCloseable.
     * This makes cleanup automatic!
     */
    public static void tryWithResourcesPattern() {
        // Java 19+ version:
        // try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
        //     executor.submit(() -> doWork());
        // } // Automatic shutdown on exit!
        
        // Pre-Java 19 equivalent:
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            executor.submit(() -> System.out.println("Working..."));
        } finally {
            shutdownAndAwaitTermination(executor);
        }
    }
    
    /**
     * A reusable Worker that can be gracefully shutdown.
     */
    public static class GracefulWorker {
        private final ExecutorService executor;
        private volatile boolean running = true;
        
        public GracefulWorker(int threads) {
            this.executor = Executors.newFixedThreadPool(threads);
        }
        
        public void submit(Runnable task) {
            if (!running) {
                throw new IllegalStateException("Worker is shutdown");
            }
            executor.submit(task);
        }
        
        public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
            running = false;
            executor.shutdown();
            
            if (!executor.awaitTermination(timeout, unit)) {
                System.out.println("Forcing shutdown...");
                executor.shutdownNow();
            }
        }
        
        public boolean isRunning() {
            return running && !executor.isTerminated();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Poison Pill Demo ===");
        poisonPillExample();
    }
}
