package com.concurrency.labs.lab17;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lab 17: Thread Dump Analysis
 * 
 * TODO: Learn to capture and interpret thread dumps.
 * 
 * 📝 NOTE: Thread dumps show the state of all threads at a point in time.
 *   Essential for debugging:
 *   - Deadlocks
 *   - High CPU usage (busy threads)
 *   - Hanging applications (blocked/waiting threads)
 * 
 * How to capture a thread dump:
 *   - jstack <pid>
 *   - kill -3 <pid> (Unix)
 *   - VisualVM or JConsole
 *   - Thread.getAllStackTraces() in code
 */
public class ThreadDumpDemo {
    
    private final Lock lock1 = new ReentrantLock();
    private final Lock lock2 = new ReentrantLock();
    
    /**
     * Creates contention that will show in thread dump.
     * 
     * 💡 THINK: In a thread dump, contended threads show as BLOCKED.
     */
    public void createContention() {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                lock1.lock();
                try {
                    // Hold lock briefly
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock1.unlock();
                }
            }
        }, "Worker-1");
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                lock1.lock();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock1.unlock();
                }
            }
        }, "Worker-2");
        
        t1.start();
        t2.start();
    }
    
    /**
     * TODO: Print a thread dump programmatically.
     * 
     * 📝 NOTE: This shows how thread dumps work internally.
     * In production, use jstack or monitoring tools.
     */
    public static void printThreadDump() {
        System.out.println("=== THREAD DUMP ===");
        System.out.println("Time: " + java.time.Instant.now());
        System.out.println();
        
        // Get all threads and their stack traces
        Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
            System.out.println("\"" + thread.getName() + "\" " + 
                             "State: " + thread.getState());
            
            // 💡 THINK: What does each state mean?
            // RUNNABLE: Executing or ready to execute
            // BLOCKED: Waiting for monitor lock
            // WAITING: Waiting indefinitely (wait(), join())
            // TIMED_WAITING: Waiting with timeout (sleep(), wait(ms))
            
            for (StackTraceElement element : stackTrace) {
                System.out.println("\tat " + element);
            }
            System.out.println();
        });
    }
    
    /**
     * Reading a thread dump - what to look for:
     * 
     * 📝 Example of a deadlock in thread dump:
     * 
     * "Thread-1":
     *   waiting to lock monitor 0x000001 (object 0x000002, a java.lang.Object),
     *   which is held by "Thread-2"
     * "Thread-2":
     *   waiting to lock monitor 0x000002 (object 0x000001, a java.lang.Object),
     *   which is held by "Thread-1"
     * 
     * Found 1 deadlock.
     * 
     * 📝 Example of high contention:
     * 
     * "Worker-1" BLOCKED
     *   - waiting to lock <0x00000001> (a java.util.concurrent.locks.ReentrantLock)
     * "Worker-2" BLOCKED
     *   - waiting to lock <0x00000001> (a java.util.concurrent.locks.ReentrantLock)
     * "Worker-3" BLOCKED
     *   - waiting to lock <0x00000001> (a java.util.concurrent.locks.ReentrantLock)
     * 
     * 💡 THINK: Many threads waiting on same lock = contention problem!
     */
    public static void threadDumpAnalysisTips() {
        System.out.println("Thread Dump Analysis Tips:");
        System.out.println("1. Look for BLOCKED threads - sign of lock contention");
        System.out.println("2. Look for 'Found N deadlock' message at the end");
        System.out.println("3. Many WAITING threads on same object = bottleneck");
        System.out.println("4. RUNNABLE threads all in same method = hot spot");
        System.out.println("5. High number of threads might indicate thread leak");
    }
    
    /**
     * TODO: Detect deadlock programmatically (advanced).
     * 
     * 🔑 HINT: Use ThreadMXBean for deadlock detection.
     */
    public static void detectDeadlock() {
        java.lang.management.ThreadMXBean bean = 
            java.lang.management.ManagementFactory.getThreadMXBean();
        
        long[] deadlockedThreadIds = bean.findDeadlockedThreads();
        
        if (deadlockedThreadIds != null) {
            System.out.println("DEADLOCK DETECTED!");
            java.lang.management.ThreadInfo[] infos = 
                bean.getThreadInfo(deadlockedThreadIds, true, true);
            
            for (java.lang.management.ThreadInfo info : infos) {
                System.out.println(info);
            }
        } else {
            System.out.println("No deadlock detected");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        ThreadDumpDemo demo = new ThreadDumpDemo();
        demo.createContention();
        
        // Let contention build up
        Thread.sleep(100);
        
        // Capture thread dump
        printThreadDump();
        
        // Check for deadlock
        detectDeadlock();
    }
}
