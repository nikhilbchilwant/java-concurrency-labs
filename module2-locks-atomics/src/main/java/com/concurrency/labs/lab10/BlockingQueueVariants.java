package com.concurrency.labs.lab10;

import java.util.concurrent.*;

/**
 * Lab 10: Blocking Queue Variants
 * 
 * TODO: Understand different BlockingQueue implementations and when to use each.
 * 
 * 📝 NOTE: BlockingQueue is the foundation of producer-consumer pattern.
 *   - put(e): Blocks if queue is full
 *   - take(): Blocks if queue is empty
 *   - offer(e, timeout): Returns false if full after timeout
 *   - poll(timeout): Returns null if empty after timeout
 * 
 * 💡 THINK: Choosing the right queue:
 *   - ArrayBlockingQueue: Bounded, fair ordering option
 *   - LinkedBlockingQueue: Optionally bounded, higher throughput
 *   - SynchronousQueue: Zero capacity, direct handoff
 *   - PriorityBlockingQueue: Unbounded, priority ordering
 */
public class BlockingQueueVariants {
    
    /**
     * ArrayBlockingQueue - Fixed capacity, backed by array.
     * 
     * 📝 NOTE: Characteristics:
     *   - Fixed capacity (must specify at creation)
     *   - Fair mode available (FIFO order for waiting threads)
     *   - Single lock for both put and take
     * 
     * 💡 THINK: When to use?
     *   - When you need bounded queue with predictable memory
     *   - When fairness is important
     */
    public static BlockingQueue<String> createArrayBlockingQueue() {
        // Constructor: capacity, fairness (optional)
        // 🔑 HINT: Fair=true means threads acquire lock in FIFO order
        //   Trade-off: Fair is slower but prevents starvation
        return new ArrayBlockingQueue<>(100, true);
    }
    
    /**
     * LinkedBlockingQueue - Optionally bounded, linked nodes.
     * 
     * 📝 NOTE: Characteristics:
     *   - Can be unbounded (default) or bounded
     *   - Separate locks for put and take (higher throughput!)
     *   - Higher memory overhead per element (linked nodes)
     * 
     * ⚠️ AVOID: Unbounded queue in production!
     *   If producers are faster than consumers → OutOfMemoryError!
     */
    public static BlockingQueue<String> createLinkedBlockingQueue() {
        // Bounded version - specify capacity
        return new LinkedBlockingQueue<>(1000);
        
        // ⚠️ Unbounded - dangerous in production!
        // return new LinkedBlockingQueue<>();
    }
    
    /**
     * SynchronousQueue - Zero capacity, direct handoff.
     * 
     * 📝 NOTE: Characteristics:
     *   - No internal capacity - each put must wait for a take!
     *   - Direct producer-to-consumer handoff
     *   - Used by Executors.newCachedThreadPool()
     * 
     * 💡 THINK: When to use?
     *   - When you want immediate handoff (no buffering)
     *   - When producers should only produce when consumers are ready
     */
    public static BlockingQueue<String> createSynchronousQueue() {
        // Fair mode ensures FIFO handoff
        return new SynchronousQueue<>(true);
    }
    
    /**
     * PriorityBlockingQueue - Unbounded, priority ordering.
     * 
     * 📝 NOTE: Characteristics:
     *   - Unbounded (grows automatically)
     *   - Elements ordered by priority (Comparable or Comparator)
     *   - take() always returns highest priority element
     * 
     * ⚠️ AVOID: Unbounded nature can cause OOM under load!
     */
    public static BlockingQueue<Task> createPriorityBlockingQueue() {
        // Elements must be Comparable or provide Comparator
        return new PriorityBlockingQueue<>();
    }
    
    /**
     * Example priority task.
     */
    public static class Task implements Comparable<Task> {
        private final int priority;
        private final String name;
        
        public Task(int priority, String name) {
            this.priority = priority;
            this.name = name;
        }
        
        @Override
        public int compareTo(Task other) {
            // Higher priority = should come first
            return Integer.compare(other.priority, this.priority);
        }
        
        @Override
        public String toString() {
            return name + " (priority=" + priority + ")";
        }
    }
    
    /**
     * TODO: Implement a producer-consumer using BlockingQueue.
     * 
     * 📝 NOTE: The beauty of BlockingQueue:
     *   - No explicit synchronization needed!
     *   - Queue handles all thread coordination
     */
    public static void producerConsumerDemo() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    String item = "Item-" + i;
                    queue.put(item);  // Blocks if full
                    System.out.println("Produced: " + item);
                }
                queue.put("DONE");  // Poison pill to signal end
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String item = queue.take();  // Blocks if empty
                    if ("DONE".equals(item)) {
                        break;  // Poison pill received
                    }
                    System.out.println("Consumed: " + item);
                    Thread.sleep(100);  // Simulate processing
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("Done!");
    }
    
    /**
     * TODO: Compare offer/poll (non-blocking) vs put/take (blocking).
     * 
     * 💡 THINK: When to use which?
     *   - put/take: When you want to wait indefinitely
     *   - offer/poll with timeout: When you want bounded waiting
     *   - offer/poll without timeout: When you want immediate response
     */
    public static void blockingVsNonBlocking(BlockingQueue<String> queue) 
            throws InterruptedException {
        
        // Non-blocking - returns immediately
        boolean added = queue.offer("item");  // false if full
        String item = queue.poll();           // null if empty
        
        // Non-blocking with timeout
        boolean addedWithTimeout = queue.offer("item", 1, TimeUnit.SECONDS);
        String itemWithTimeout = queue.poll(1, TimeUnit.SECONDS);
        
        // Blocking - waits forever
        queue.put("item");    // Waits if full
        String taken = queue.take();  // Waits if empty
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== BlockingQueue Producer-Consumer Demo ===");
        producerConsumerDemo();
    }
}
