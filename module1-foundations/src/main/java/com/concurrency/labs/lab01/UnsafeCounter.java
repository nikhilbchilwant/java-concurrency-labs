package com.concurrency.labs.lab01;

/**
 * Lab 01: Race Condition - The Unsafe Counter
 * 
 * This class demonstrates a classic race condition.
 * Run the test to see inconsistent results!
 * 
 * 📝 NOTE: A race condition occurs when multiple threads access shared
 * mutable state without proper synchronization, leading to unpredictable results.
 */
public class UnsafeCounter {
    
    // ⚠️ AVOID: This field is accessed by multiple threads without synchronization
    // 💡 THINK: What can go wrong with count++ when two threads run it simultaneously?
    private int count = 0;
    
    /**
     * Increments the counter.
     * 
     * 📝 NOTE: count++ is NOT atomic! It involves:
     *   1. READ the current value
     *   2. ADD 1 to it
     *   3. WRITE the new value back
     * 
     * 💡 THINK: If Thread A reads count=5, then Thread B reads count=5,
     * both increment to 6 and write... what's the final value?
     */
    public void increment() {
        count++;
    }
    
    public int getCount() {
        return count;
    }
}
