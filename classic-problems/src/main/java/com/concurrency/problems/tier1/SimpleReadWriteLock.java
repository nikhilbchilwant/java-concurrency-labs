package com.concurrency.problems.tier1;

/**
 * Classic Problem #2: Custom Reader-Writer Lock
 * 
 * TODO: Implement a reader-writer lock from scratch.
 * 
 * 📝 NOTE: Rules for reader-writer locks:
 *   - Multiple readers can hold the lock simultaneously
 *   - Only one writer can hold the lock (exclusive)
 *   - Writers and readers are mutually exclusive
 * 
 * ⚠️ AVOID: Writer starvation!
 *   If readers keep coming, writers might wait forever.
 *   
 * 💡 THINK: How would you implement "writer preference"?
 *   When a writer is waiting, new readers should block too!
 * 
 * @see java.util.concurrent.locks.ReentrantReadWriteLock for production use
 */
public class SimpleReadWriteLock {
    
    private int readers = 0;        // Number of active readers
    private int writers = 0;        // Number of active writers (0 or 1)
    private int writeRequests = 0;  // Number of waiting writers
    
    // 💡 THINK: Why track writeRequests separately from writers?
    // This lets us implement writer preference to prevent starvation!
    
    /**
     * TODO: Acquire the read lock.
     * 
     * 🔑 HINT: Readers can proceed if:
     *   - No active writers (writers == 0)
     *   - No waiting writers (writeRequests == 0) - for writer preference
     * 
     * 📝 NOTE: Without the writeRequests check, readers could starve writers!
     */
    public synchronized void lockRead() throws InterruptedException {
        // TODO: Implement read lock acquisition
        // Step 1: While there are writers OR waiting writers, wait
        // Step 2: Increment readers count
        
        // ⚠️ AVOID: This simple version allows reader starvation of writers:
        // while (writers > 0) { wait(); }
        // 
        // Better: Also check writeRequests to give writers priority
        while (writers > 0 || writeRequests > 0) {
            wait();
        }
        readers++;
    }
    
    /**
     * TODO: Release the read lock.
     * 
     * 📝 NOTE: When the last reader unlocks, notify waiting writers!
     */
    public synchronized void unlockRead() {
        // TODO: Implement read lock release
        readers--;
        if (readers == 0) {
            notifyAll(); // Wake up waiting writers
        }
    }
    
    /**
     * TODO: Acquire the write lock.
     * 
     * 🔑 HINT: Writers must wait for:
     *   - All readers to finish (readers == 0)
     *   - Any active writer to finish (writers == 0)
     * 
     * 💡 THINK: Why increment writeRequests before waiting?
     *   This signals to lockRead() that a writer is waiting!
     */
    public synchronized void lockWrite() throws InterruptedException {
        // TODO: Implement write lock acquisition
        writeRequests++;
        try {
            while (readers > 0 || writers > 0) {
                wait();
            }
            writers++;
        } finally {
            writeRequests--;
        }
    }
    
    /**
     * TODO: Release the write lock.
     */
    public synchronized void unlockWrite() {
        // TODO: Implement write lock release
        writers--;
        notifyAll(); // Wake up ALL waiting readers and writers
        
        // 💡 THINK: Could we use notify() instead of notifyAll()?
        // What would happen if we woke only one waiting thread?
    }
    
    // Diagnostic methods
    public synchronized int getReaderCount() {
        return readers;
    }
    
    public synchronized int getWriterCount() {
        return writers;
    }
    
    public synchronized int getWriteRequestCount() {
        return writeRequests;
    }
}
