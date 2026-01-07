package com.concurrency.labs.lab05;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lab 05: ReentrantLock with Conditions
 * 
 * TODO: Implement a bounded buffer using ReentrantLock and Condition.
 * 
 * 📝 NOTE: Why use Lock/Condition over synchronized/wait/notify?
 *   1. Multiple Conditions: Separate waitsets for producers and consumers
 *   2. Interruptible: lockInterruptibly() can be interrupted
 *   3. Timed: tryLock(timeout) returns false instead of blocking forever
 *   4. Fairness: Can ensure FIFO ordering of waiting threads
 * 
 * 💡 THINK: With synchronized, notifyAll() wakes ALL waiting threads.
 *   With Condition, you can wake ONLY producers or ONLY consumers!
 * 
 * ⚠️ AVOID: Forgetting to unlock in a finally block - this causes deadlock!
 *   Always use try-finally pattern:
 *   lock.lock();
 *   try { ... } finally { lock.unlock(); }
 * 
 * @param <E> the type of elements in the buffer
 */
public class BoundedBufferWithLock<E> {
    
    private final Object[] items;
    private int head, tail, count;
    
    // TODO: Create a ReentrantLock
    // 🔑 HINT: new ReentrantLock() or new ReentrantLock(true) for fair lock
    // 💡 THINK: What's the tradeoff with fair locks? (Hint: performance)
    private final ReentrantLock lock = new ReentrantLock();
    
    // TODO: Create two Conditions - one for "not full", one for "not empty"
    // 🔑 HINT: Condition notFull = lock.newCondition();
    // 📝 NOTE: Each Condition has its own wait queue!
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    
    public BoundedBufferWithLock(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        items = new Object[capacity];
    }
    
    /**
     * TODO: Add an item, blocking if full.
     * 
     * 🔑 HINT: Use notFull.await() instead of wait()
     *          Use notEmpty.signal() instead of notifyAll()
     * 
     * 💡 THINK: Why signal() instead of signalAll()?
     *   - notEmpty.signal() wakes exactly ONE waiting consumer
     *   - This is more efficient than waking all of them!
     */
    public void put(E item) throws InterruptedException {
        lock.lock();
        try {
            // TODO: While full, await on notFull condition
            // TODO: Add item
            // TODO: Signal notEmpty condition (wake one consumer)
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * TODO: Remove and return an item, blocking if empty.
     */
    @SuppressWarnings("unchecked")
    public E take() throws InterruptedException {
        lock.lock();
        try {
            // TODO: While empty, await on notEmpty condition
            // TODO: Remove item
            // TODO: Signal notFull condition (wake one producer)
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
