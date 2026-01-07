package com.concurrency.problems.tier2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Classic Problem #4: Print In Order
 * 
 * Three methods: first(), second(), third() are called by three different threads.
 * Ensure they execute in order: first → second → third.
 * 
 * 📝 NOTE: This tests thread orchestration beyond simple locking.
 * 
 * 💡 THINK: Multiple solutions exist:
 *   1. CountDownLatch - simple, one-time use
 *   2. Semaphore - reusable, more flexible
 *   3. volatile flags + busy wait - works but inefficient
 */
public class PrintInOrder {
    
    // Solution 1: Using CountDownLatch
    // 📝 NOTE: CountDownLatch is initialized with a count.
    //   await() blocks until count reaches 0.
    //   countDown() decrements the count.
    private final CountDownLatch firstDone = new CountDownLatch(1);
    private final CountDownLatch secondDone = new CountDownLatch(1);
    
    /**
     * TODO: Print "first" - no waiting needed.
     * After printing, signal that first is done.
     */
    public void first(Runnable printFirst) throws InterruptedException {
        // No need to wait - first runs immediately
        printFirst.run();
        
        // Signal that first is done
        // 🔑 HINT: firstDone.countDown();
        firstDone.countDown();
    }
    
    /**
     * TODO: Wait for first to complete, then print "second".
     * After printing, signal that second is done.
     */
    public void second(Runnable printSecond) throws InterruptedException {
        // Wait for first to complete
        // 🔑 HINT: firstDone.await();
        firstDone.await();
        
        // Now safe to run second
        printSecond.run();
        
        // Signal that second is done
        secondDone.countDown();
    }
    
    /**
     * TODO: Wait for second to complete, then print "third".
     */
    public void third(Runnable printThird) throws InterruptedException {
        // Wait for second to complete
        // 🔑 HINT: secondDone.await();
        secondDone.await();
        
        // Now safe to run third
        printThird.run();
    }
}

/**
 * Alternative solution using Semaphore.
 * 
 * 💡 THINK: When to use Semaphore vs CountDownLatch?
 *   - CountDownLatch: One-time event, cannot be reset
 *   - Semaphore: Reusable, can acquire/release multiple times
 */
class PrintInOrderSemaphore {
    
    // Initialize with 0 permits - threads must wait
    private final Semaphore runSecond = new Semaphore(0);
    private final Semaphore runThird = new Semaphore(0);
    
    public void first(Runnable printFirst) throws InterruptedException {
        printFirst.run();
        runSecond.release(); // Give second permission to run
    }
    
    public void second(Runnable printSecond) throws InterruptedException {
        runSecond.acquire(); // Wait for permission
        printSecond.run();
        runThird.release(); // Give third permission to run
    }
    
    public void third(Runnable printThird) throws InterruptedException {
        runThird.acquire(); // Wait for permission
        printThird.run();
    }
}
