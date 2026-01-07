package com.concurrency.labs.lab15;

/**
 * Lab 15: Deadlock Demonstration and Prevention
 * 
 * This class intentionally creates a deadlock for educational purposes.
 * 
 * 📝 NOTE: Deadlock requires ALL four conditions:
 *   1. Mutual Exclusion: Resources can't be shared
 *   2. Hold and Wait: Holding one resource while waiting for another
 *   3. No Preemption: Can't forcibly take a resource from a thread
 *   4. Circular Wait: A waits for B, B waits for A
 * 
 * To prevent deadlock, break at least one condition!
 * 
 * ⚠️ WARNING: This code WILL deadlock! Don't use in production!
 */
public class DeadlockDemo {
    
    private final Object lockA = new Object();
    private final Object lockB = new Object();
    
    /**
     * This creates a deadlock scenario.
     * 
     * Thread 1: lockA → tries lockB
     * Thread 2: lockB → tries lockA
     * 
     * Result: Both threads wait forever!
     */
    public void createDeadlock() {
        Thread thread1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("Thread 1: Holding lockA");
                try {
                    Thread.sleep(100); // Give Thread 2 time to acquire lockB
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("Thread 1: Waiting for lockB...");
                synchronized (lockB) {
                    System.out.println("Thread 1: Acquired lockB");
                }
            }
        }, "Thread-1");
        
        Thread thread2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("Thread 2: Holding lockB");
                try {
                    Thread.sleep(100); // Give Thread 1 time to acquire lockA
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("Thread 2: Waiting for lockA...");
                synchronized (lockA) {
                    System.out.println("Thread 2: Acquired lockA");
                }
            }
        }, "Thread-2");
        
        thread1.start();
        thread2.start();
        
        // These joins will never complete - deadlock!
        // ⚠️ Don't run this without a timeout!
    }
}
