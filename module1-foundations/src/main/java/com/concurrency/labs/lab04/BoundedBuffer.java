package com.concurrency.labs.lab04;

/**
 * Lab 04: Bounded Buffer with Wait/Notify
 * 
 * TODO: Implement a thread-safe bounded buffer using wait/notify.
 * 
 * 📝 NOTE: This is the classic Producer-Consumer pattern!
 *   - put() blocks when buffer is full
 *   - take() blocks when buffer is empty
 * 
 * ⚠️ CRITICAL: Use WHILE loops, not IF statements, for wait conditions!
 *   Why? Because of SPURIOUS WAKEUPS - a thread can wake up without being notified.
 *   Also, multiple consumers might wake up but only one should proceed.
 * 
 * 💡 THINK: Why use notifyAll() instead of notify()?
 *   - notify() wakes ONE waiting thread (could be wrong one!)
 *   - notifyAll() wakes ALL waiting threads (safer but more overhead)
 *   - With separate conditions (Lock/Condition), you can be more precise
 * 
 * @param <E> the type of elements in the buffer
 */
public class BoundedBuffer<E> {
    
    private final Object[] items;
    private int head;      // Index for next take
    private int tail;      // Index for next put
    private int count;     // Number of items in buffer
    
    public BoundedBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        items = new Object[capacity];
    }
    
    /**
     * TODO: Add an item to the buffer, blocking if full.
     * 
     * 🔑 HINT: The pattern is:
     *   synchronized(lock) {
     *       while (isFull()) {  // WHILE, not IF!
     *           wait();
     *       }
     *       // add the item
     *       notifyAll();  // wake up waiting consumers
     *   }
     * 
     * 📝 NOTE: wait() releases the lock and suspends the thread.
     * When notified, it re-acquires the lock before continuing.
     * 
     * @param item the item to add
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void put(E item) throws InterruptedException {
        // TODO: Implement blocking put
        // Step 1: While buffer is full, wait()
        // Step 2: Add item at tail position
        // Step 3: Update tail index (circular: tail = (tail + 1) % capacity)
        // Step 4: Increment count
        // Step 5: notifyAll() to wake waiting consumers
    }
    
    /**
     * TODO: Remove and return an item, blocking if empty.
     * 
     * 🔑 HINT: Similar pattern to put(), but check for empty instead of full.
     * 
     * 💡 THINK: What happens if you use notify() instead of notifyAll()?
     *   Consider: 2 producers waiting, 2 consumers waiting.
     *   If one consumer takes an item and calls notify(), it might wake
     *   another consumer instead of a producer!
     * 
     * @return the removed item
     * @throws InterruptedException if interrupted while waiting
     */
    @SuppressWarnings("unchecked")
    public synchronized E take() throws InterruptedException {
        // TODO: Implement blocking take
        // Step 1: While buffer is empty, wait()
        // Step 2: Get item at head position
        // Step 3: Clear the slot (helps GC)
        // Step 4: Update head index (circular)
        // Step 5: Decrement count
        // Step 6: notifyAll() to wake waiting producers
        // Step 7: Return the item
        return null;
    }
    
    public synchronized int size() {
        return count;
    }
    
    public int capacity() {
        return items.length;
    }
    
    public synchronized boolean isEmpty() {
        return count == 0;
    }
    
    public synchronized boolean isFull() {
        return count == items.length;
    }
}
