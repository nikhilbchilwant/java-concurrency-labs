package com.concurrency.problems.tier3;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classic Problem #9: Delayed Task Scheduler
 * 
 * TODO: Implement a scheduler that executes tasks after a delay.
 * 
 * 📝 NOTE: Similar to ScheduledExecutorService but built from scratch.
 * 
 * Key components:
 *   1. PriorityQueue ordered by execution time
 *   2. Worker thread that waits for the next task
 *   3. Condition.awaitNanos() for efficient waiting
 * 
 * ⚠️ AVOID: Busy waiting! Don't poll the queue in a tight loop.
 * 
 * 💡 THINK: How to wait efficiently?
 *   Calculate time until next task, then wait exactly that long.
 */
public class DelayedTaskScheduler {
    
    /**
     * A scheduled task wrapper.
     */
    public static class ScheduledTask implements Comparable<ScheduledTask> {
        private final Runnable task;
        private final long executeAtNanos;
        
        public ScheduledTask(Runnable task, long delayNanos) {
            this.task = task;
            this.executeAtNanos = System.nanoTime() + delayNanos;
        }
        
        public long getDelay() {
            return executeAtNanos - System.nanoTime();
        }
        
        public void run() {
            task.run();
        }
        
        @Override
        public int compareTo(ScheduledTask other) {
            // Earlier tasks should come first
            return Long.compare(this.executeAtNanos, other.executeAtNanos);
        }
    }
    
    private final PriorityBlockingQueue<ScheduledTask> queue;
    private final ReentrantLock lock;
    private final Condition available;
    private final Thread worker;
    private volatile boolean running;
    
    public DelayedTaskScheduler() {
        this.queue = new PriorityBlockingQueue<>();
        this.lock = new ReentrantLock();
        this.available = lock.newCondition();
        this.running = true;
        
        // Start worker thread
        this.worker = new Thread(this::runWorker, "Scheduler-Worker");
        this.worker.start();
    }
    
    /**
     * TODO: Schedule a task to run after the specified delay.
     * 
     * 🔑 HINT: Add to queue and signal the worker if this might be
     * the new earliest task.
     */
    public void schedule(Runnable task, long delay, TimeUnit unit) {
        long delayNanos = unit.toNanos(delay);
        ScheduledTask scheduled = new ScheduledTask(task, delayNanos);
        
        lock.lock();
        try {
            queue.add(scheduled);
            
            // Signal worker in case this is earlier than current wait
            // 📝 NOTE: Worker might be waiting for a later task
            available.signal();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * TODO: Worker loop that executes tasks at their scheduled time.
     * 
     * 🔑 HINT - The pattern:
     *   while (running) {
     *       lock.lock();
     *       try {
     *           while (queue is empty) {
     *               available.await();  // Wait for tasks
     *           }
     *           
     *           ScheduledTask task = queue.peek();
     *           long delay = task.getDelay();
     *           
     *           if (delay > 0) {
     *               available.awaitNanos(delay);  // Wait until execution time
     *           } else {
     *               queue.poll();  // Remove and execute
     *               lock.unlock();
     *               task.run();    // Run outside lock!
     *               continue;
     *           }
     *       } finally {
     *           lock.unlock();
     *       }
     *   }
     */
    private void runWorker() {
        while (running) {
            lock.lock();
            try {
                // Wait for tasks
                while (queue.isEmpty() && running) {
                    available.await();
                }
                
                if (!running) break;
                
                ScheduledTask task = queue.peek();
                if (task == null) continue;
                
                long delay = task.getDelay();
                
                if (delay > 0) {
                    // 💡 THINK: awaitNanos is the key to efficient waiting!
                    // No busy-wait - thread sleeps until woken or timeout
                    available.awaitNanos(delay);
                } else {
                    // Time to execute!
                    queue.poll();
                    
                    // ⚠️ CRITICAL: Release lock before running task!
                    lock.unlock();
                    try {
                        task.run();
                    } finally {
                        // Re-acquire for next iteration
                        lock.lock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * Shutdown the scheduler.
     */
    public void shutdown() {
        running = false;
        lock.lock();
        try {
            available.signal();
        } finally {
            lock.unlock();
        }
        
        try {
            worker.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        DelayedTaskScheduler scheduler = new DelayedTaskScheduler();
        
        System.out.println("Scheduling tasks...");
        
        scheduler.schedule(() -> System.out.println("Task 1 (3s delay) at " + System.currentTimeMillis()), 
                          3, TimeUnit.SECONDS);
        scheduler.schedule(() -> System.out.println("Task 2 (1s delay) at " + System.currentTimeMillis()), 
                          1, TimeUnit.SECONDS);
        scheduler.schedule(() -> System.out.println("Task 3 (2s delay) at " + System.currentTimeMillis()), 
                          2, TimeUnit.SECONDS);
        
        System.out.println("Scheduled at " + System.currentTimeMillis());
        
        Thread.sleep(5000);
        
        scheduler.shutdown();
        System.out.println("Scheduler shutdown");
    }
}
