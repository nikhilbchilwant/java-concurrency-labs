package com.concurrency.labs.lab03;

/**
 * Lab 03: Thread Lifecycle and States
 * 
 * TODO: Demonstrate all thread states by creating scenarios that put
 * threads into each state.
 * 
 * 📝 NOTE: Thread states in Java (Thread.State enum):
 *   - NEW: Created but not started
 *   - RUNNABLE: Executing or ready to execute
 *   - BLOCKED: Waiting to acquire a monitor lock
 *   - WAITING: Waiting indefinitely (wait(), join(), park())
 *   - TIMED_WAITING: Waiting with timeout (sleep(), wait(timeout), join(timeout))
 *   - TERMINATED: Completed execution
 * 
 * 💡 THINK: What's the difference between BLOCKED and WAITING?
 *   - BLOCKED: Trying to enter a synchronized block held by another thread
 *   - WAITING: Explicitly waiting for a signal (notify) or another thread to complete
 */
public class ThreadStates {
    
    private final Object lock = new Object();
    
    /**
     * TODO: Create and return a thread in NEW state (not started).
     */
    public Thread createNewThread() {
        // TODO: Create a Thread but don't call start()
        // 🔑 HINT: new Thread(() -> {...}) creates a thread in NEW state
        return null;
    }
    
    /**
     * TODO: Create a thread that will be in TIMED_WAITING state.
     * 
     * 🔑 HINT: Thread.sleep() puts the thread in TIMED_WAITING state.
     */
    public Thread createTimedWaitingThread() {
        // TODO: Create and start a thread that sleeps
        return null;
    }
    
    /**
     * TODO: Create a thread that will be in WAITING state.
     * 
     * 🔑 HINT: Object.wait() without timeout puts the thread in WAITING state.
     * 📝 NOTE: wait() must be called inside a synchronized block!
     */
    public Thread createWaitingThread() {
        // TODO: Create and start a thread that calls wait()
        return null;
    }
    
    /**
     * TODO: Create a thread that will be in BLOCKED state.
     * 
     * 🔑 HINT: You need TWO threads - one holding a lock, another trying to acquire it.
     * 💡 THINK: How would you reliably put a thread in BLOCKED state for observation?
     */
    public Thread createBlockedThread() {
        // TODO: First acquire the lock in another thread, then try to acquire it
        return null;
    }
}
