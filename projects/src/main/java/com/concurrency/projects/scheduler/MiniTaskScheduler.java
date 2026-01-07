package com.concurrency.projects.scheduler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

/**
 * Project 3: Mini Task Scheduler
 * 
 * TODO: Build a scheduler that supports delayed and periodic tasks.
 * 
 * 📝 NOTE: This integrates:
 *   - PriorityQueue for task ordering
 *   - Condition.awaitNanos for efficient waiting
 *   - Cancellation support
 *   - Periodic task rescheduling
 * 
 * Like java.util.concurrent.ScheduledThreadPoolExecutor, but from scratch!
 * 
 * 💡 THINK: Key challenges:
 *   - Efficiently waiting for the next task
 *   - Handling new tasks with earlier deadlines
 *   - Cancellation without memory leaks
 */
public class MiniTaskScheduler {
    
    private final PriorityBlockingQueue<ScheduledTask> taskQueue;
    private final Thread[] workers;
    private final Lock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private volatile boolean running = true;
    private final AtomicLong taskIdGenerator = new AtomicLong(0);
    
    /**
     * A scheduled task.
     */
    public class ScheduledTask implements Comparable<ScheduledTask>, Future<Object> {
        private final long id;
        private final Runnable command;
        private volatile long nextExecutionTime;
        private final long period; // 0 for one-shot tasks
        private volatile boolean cancelled = false;
        private volatile boolean done = false;
        private final CountDownLatch completionLatch = new CountDownLatch(1);
        
        ScheduledTask(Runnable command, long delayNanos, long periodNanos) {
            this.id = taskIdGenerator.incrementAndGet();
            this.command = command;
            this.nextExecutionTime = System.nanoTime() + delayNanos;
            this.period = periodNanos;
        }
        
        void run() {
            if (cancelled) return;
            
            try {
                command.run();
            } catch (Exception e) {
                System.err.println("Task " + id + " failed: " + e.getMessage());
            }
            
            if (period > 0 && !cancelled) {
                // Reschedule periodic task
                nextExecutionTime = System.nanoTime() + period;
                taskQueue.add(this);
                signalAvailable();
            } else {
                done = true;
                completionLatch.countDown();
            }
        }
        
        long getDelay() {
            return nextExecutionTime - System.nanoTime();
        }
        
        @Override
        public int compareTo(ScheduledTask other) {
            return Long.compare(this.nextExecutionTime, other.nextExecutionTime);
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            done = true;
            completionLatch.countDown();
            return true;
        }
        
        @Override
        public boolean isCancelled() {
            return cancelled;
        }
        
        @Override
        public boolean isDone() {
            return done;
        }
        
        @Override
        public Object get() throws InterruptedException {
            completionLatch.await();
            return null;
        }
        
        @Override
        public Object get(long timeout, TimeUnit unit) 
                throws InterruptedException, TimeoutException {
            if (!completionLatch.await(timeout, unit)) {
                throw new TimeoutException();
            }
            return null;
        }
    }
    
    /**
     * Creates a scheduler with the specified number of worker threads.
     */
    public MiniTaskScheduler(int numWorkers) {
        this.taskQueue = new PriorityBlockingQueue<>();
        this.workers = new Thread[numWorkers];
        
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Thread(this::workerLoop, "Scheduler-Worker-" + i);
            workers[i].start();
        }
    }
    
    /**
     * TODO: Schedule a one-shot task with delay.
     */
    public ScheduledTask schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledTask task = new ScheduledTask(command, unit.toNanos(delay), 0);
        taskQueue.add(task);
        signalAvailable();
        return task;
    }
    
    /**
     * TODO: Schedule a periodic task.
     */
    public ScheduledTask scheduleAtFixedRate(Runnable command, 
                                              long initialDelay, 
                                              long period, 
                                              TimeUnit unit) {
        ScheduledTask task = new ScheduledTask(
            command, 
            unit.toNanos(initialDelay), 
            unit.toNanos(period)
        );
        taskQueue.add(task);
        signalAvailable();
        return task;
    }
    
    /**
     * TODO: Worker thread loop.
     * 
     * 🔑 HINT: The pattern:
     *   1. Wait for a task to be available
     *   2. If task's time hasn't come, wait with awaitNanos
     *   3. Execute the task
     */
    private void workerLoop() {
        while (running) {
            lock.lock();
            try {
                // Wait for tasks
                while (taskQueue.isEmpty() && running) {
                    available.await();
                }
                
                if (!running) break;
                
                ScheduledTask task = taskQueue.peek();
                if (task == null) continue;
                
                long delay = task.getDelay();
                
                if (delay > 0) {
                    // Wait for the task's scheduled time
                    available.awaitNanos(delay);
                } else {
                    // Time to execute!
                    taskQueue.poll();
                    
                    if (!task.cancelled) {
                        // Release lock while executing
                        lock.unlock();
                        try {
                            task.run();
                        } finally {
                            lock.lock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }
    
    private void signalAvailable() {
        lock.lock();
        try {
            available.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Shutdown the scheduler.
     */
    public void shutdown() {
        running = false;
        signalAvailable();
        
        for (Thread worker : workers) {
            worker.interrupt();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Example usage.
     */
    public static void main(String[] args) throws Exception {
        MiniTaskScheduler scheduler = new MiniTaskScheduler(2);
        
        System.out.println("Scheduling tasks...");
        
        // One-shot delayed task
        ScheduledTask delayed = scheduler.schedule(() -> {
            System.out.println("Delayed task executed at " + System.currentTimeMillis());
        }, 2, TimeUnit.SECONDS);
        
        // Periodic task
        ScheduledTask periodic = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Periodic task at " + System.currentTimeMillis());
        }, 500, 1000, TimeUnit.MILLISECONDS);
        
        // Let it run for a while
        Thread.sleep(5000);
        
        // Cancel periodic task
        periodic.cancel(false);
        System.out.println("Periodic task cancelled");
        
        // Wait for delayed task
        delayed.get();
        System.out.println("Delayed task completed");
        
        scheduler.shutdown();
        System.out.println("Scheduler shutdown");
    }
}
