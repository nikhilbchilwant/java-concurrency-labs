package com.concurrency.labs.lab06;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lab 06: ReadWriteLock for Read-Heavy Cache
 * 
 * TODO: Implement a thread-safe cache using ReadWriteLock.
 * 
 * 📝 NOTE: ReadWriteLock allows:
 *   - Multiple readers concurrently (shared access)
 *   - Only one writer at a time (exclusive access)
 *   - Writers block readers and other writers
 * 
 * 💡 THINK: When is ReadWriteLock better than synchronized?
 *   - When reads are MUCH more frequent than writes
 *   - synchronized blocks ALL access during both reads and writes
 *   - ReadWriteLock allows concurrent reads!
 * 
 * ⚠️ AVOID: Holding read lock while trying to acquire write lock - DEADLOCK!
 *   (Read locks are not upgradeable to write locks)
 * 
 * @param <K> key type
 * @param <V> value type
 */
public class ReadWriteCache<K, V> {
    
    private final Map<K, V> cache = new HashMap<>();
    
    // TODO: Create a ReadWriteLock
    // 🔑 HINT: ReentrantReadWriteLock() or ReentrantReadWriteLock(true) for fair
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    /**
     * TODO: Get a value from the cache (read operation).
     * 
     * 🔑 HINT: Use rwLock.readLock().lock() and unlock()
     * 
     * 📝 NOTE: Multiple threads can hold the read lock simultaneously!
     */
    public V get(K key) {
        // TODO: Acquire read lock, try-finally, unlock
        rwLock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    /**
     * TODO: Put a value into the cache (write operation).
     * 
     * 🔑 HINT: Use rwLock.writeLock().lock() and unlock()
     * 
     * 📝 NOTE: Write lock is exclusive - no readers or writers allowed!
     */
    public void put(K key, V value) {
        // TODO: Acquire write lock, try-finally, unlock
        rwLock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    /**
     * TODO: Implement "get or compute" pattern.
     * 
     * ⚠️ AVOID: This naive approach has a race condition!
     *   - Check if key exists (read lock)
     *   - Compute and put if missing (write lock)
     *   - Between releasing read and acquiring write, another thread might put!
     * 
     * 💡 THINK: How would you implement this correctly?
     *   Option 1: Use write lock for entire operation (simple but blocks readers)
     *   Option 2: Use ConcurrentHashMap.computeIfAbsent() (better!)
     */
    public V getOrCompute(K key, java.util.function.Function<K, V> compute) {
        // TODO: Implement correctly - this is tricky!
        // 🔑 HINT: Start with write lock for simplicity, then optimize
        return null;
    }
    
    public int size() {
        rwLock.readLock().lock();
        try {
            return cache.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
