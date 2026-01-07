package com.concurrency.problems.tier4;

/**
 * Holder Pattern Singleton - Another thread-safe lazy initialization approach.
 * 
 * 📝 NOTE: This uses the "Initialization-on-demand holder" idiom.
 * 
 * How it works:
 *   - Inner class is not loaded until getInstance() is called
 *   - Class loading is guaranteed to be thread-safe by the JVM
 *   - No synchronization needed in getInstance()!
 * 
 * 💡 THINK: Compare to double-checked locking:
 *   - Holder: Simpler, no volatile/synchronized needed
 *   - Double-checked: More flexible (can pass arguments to constructor)
 * 
 * ⚠️ AVOID: Using this when you need to pass initialization parameters.
 *   The holder class can't access outer class constructor parameters.
 */
public class HolderSingleton {
    
    /**
     * Private constructor.
     */
    private HolderSingleton() {
        // Initialization logic
    }
    
    /**
     * Holder class - loaded lazily on first access to getInstance().
     * 
     * 📝 NOTE: The JVM guarantees that class initialization is thread-safe!
     * This means INSTANCE is created exactly once, without explicit synchronization.
     */
    private static class Holder {
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }
    
    /**
     * Returns the singleton instance.
     * 
     * 💡 THINK: Why is this thread-safe without synchronized?
     *   The JVM internally synchronizes class loading and initialization.
     *   The Holder class is only loaded when getInstance() is first called.
     */
    public static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }
}
