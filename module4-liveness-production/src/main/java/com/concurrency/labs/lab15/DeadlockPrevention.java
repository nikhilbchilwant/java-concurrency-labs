package com.concurrency.labs.lab15;

/**
 * Lab 15: Deadlock Prevention with Lock Ordering
 * 
 * TODO: Fix the deadlock by always acquiring locks in the same order.
 * 
 * 💡 THINK: Why does consistent lock ordering prevent deadlock?
 *   If ALL threads always acquire lockA before lockB:
 *   - Thread 1: lockA → lockB ✓
 *   - Thread 2: lockA → lockB ✓
 *   No circular wait is possible!
 * 
 * 📝 NOTE: In real code, use object identity hashcode to determine order:
 *   int hash1 = System.identityHashCode(lock1);
 *   int hash2 = System.identityHashCode(lock2);
 *   if (hash1 < hash2) { acquire lock1 then lock2 }
 *   else { acquire lock2 then lock1 }
 */
public class DeadlockPrevention {
    
    private final Object lockA = new Object();
    private final Object lockB = new Object();
    
    /**
     * TODO: Implement deadlock-free version.
     * 
     * 🔑 HINT: Both threads should acquire locks in the SAME order.
     * Simplest approach: Always acquire lockA before lockB.
     */
    public void safeOperation() {
        Thread thread1 = new Thread(() -> {
            // TODO: Always acquire lockA first, then lockB
            synchronized (lockA) {
                System.out.println("Thread 1: Holding lockA");
                sleep(100);
                
                synchronized (lockB) {
                    System.out.println("Thread 1: Holding both locks");
                    doWork();
                }
            }
        }, "Thread-1");
        
        Thread thread2 = new Thread(() -> {
            // TODO: Same order as Thread 1 - lockA first, then lockB
            // ⚠️ AVOID: Acquiring lockB first (this was the bug!)
            synchronized (lockA) {  // Changed from lockB!
                System.out.println("Thread 2: Holding lockA");
                sleep(100);
                
                synchronized (lockB) {  // Changed from lockA!
                    System.out.println("Thread 2: Holding both locks");
                    doWork();
                }
            }
        }, "Thread-2");
        
        thread1.start();
        thread2.start();
        
        // Now these joins will complete - no deadlock!
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Both threads completed successfully!");
    }
    
    private void doWork() {
        // Simulate some work
        System.out.println(Thread.currentThread().getName() + " doing work");
    }
    
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
