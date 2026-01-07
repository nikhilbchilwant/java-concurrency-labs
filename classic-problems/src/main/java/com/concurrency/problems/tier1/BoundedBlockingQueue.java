package com.concurrency.problems.tier1;

/**
 * Classic Problem #1: Bounded Blocking Queue (with wait/notify)
 * 
 * This is THE most important concurrency problem for interviews!
 * 
 * TODO: Implement a thread-safe bounded queue that:
 *   - Blocks on put() when full
 *   - Blocks on take() when empty
 *   - Supports multiple producers and consumers
 * 
 * ⚠️ CRITICAL: Common mistakes to avoid:
 *   1. Using IF instead of WHILE for wait conditions (spurious wakeups!)
 *   2. Using notify() instead of notifyAll() (wrong thread might wake up!)
 *   3. Forgetting to handle InterruptedException properly
 * 
 * 💡 THINK: After implementing this version, implement another using
 *   ReentrantLock + Condition for comparison. Which is cleaner?
 * 
 * 📝 NOTE: In production, use java.util.concurrent.ArrayBlockingQueue!
 *   This exercise is for learning the fundamentals.
 * 
 * @param <E> element type
 */
public class BoundedBlockingQueue<E> {
    
    private final Object[] items;
    private int head;      // Index of next element to remove
    private int tail;      // Index of next slot to fill
    private int count;     // Number of elements in queue
    
    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.items = new Object[capacity];
    }
    
    /**
     * TODO: Add an element, blocking if the queue is full.
     * 
     * 🔑 HINT - The correct pattern:
     * 
     *   synchronized (this) {
     *       while (count == items.length) {  // WHILE, not IF!
     *           wait();  // Release lock and wait
     *       }
     *       items[tail] = item;
     *       tail = (tail + 1) % items.length;  // Circular increment
     *       count++;
     *       notifyAll();  // Wake up waiting consumers
     *   }
     * 
     * 💡 THINK: Why must we use WHILE instead of IF?
     *   1. Spurious wakeups: Thread can wake without notify
     *   2. Multiple consumers: Another consumer might take the item first
     * 
     * @param item the element to add
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public synchronized void put(E item) throws InterruptedException {
        // TODO: Implement blocking put
        // Step 1: Wait while full (use WHILE loop!)
        // Step 2: Add item at tail
        // Step 3: Update tail (circular)
        // Step 4: Increment count
        // Step 5: notifyAll()
    }
    
    /**
     * TODO: Remove and return an element, blocking if empty.
     * 
     * 📝 NOTE: The implementation mirrors put() but checks for empty
     * instead of full, and notifies producers instead of consumers.
     * 
     * ⚠️ AVOID: Returning null to indicate empty queue!
     *   Blocking queues should block, not return null.
     *   (Unless you implement a separate poll() with timeout)
     * 
     * @return the removed element
     * @throws InterruptedException if interrupted while waiting
     */
    @SuppressWarnings("unchecked")
    public synchronized E take() throws InterruptedException {
        // TODO: Implement blocking take
        // Step 1: Wait while empty (use WHILE loop!)
        // Step 2: Get item at head
        // Step 3: Clear slot (help GC)
        // Step 4: Update head (circular)
        // Step 5: Decrement count
        // Step 6: notifyAll()
        // Step 7: Return item
        return null;
    }
    
    /**
     * Returns the number of elements in the queue.
     */
    public synchronized int size() {
        return count;
    }
    
    /**
     * Returns true if the queue is empty.
     */
    public synchronized boolean isEmpty() {
        return count == 0;
    }
    
    /**
     * Returns true if the queue is full.
     */
    public synchronized boolean isFull() {
        return count == items.length;
    }
    
    /**
     * Returns the capacity of the queue.
     */
    public int capacity() {
        return items.length;
    }
}
