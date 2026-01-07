package com.concurrency.problems.tier4;

/**
 * Classic Problem #10: Thread-Safe Singleton with Double-Checked Locking
 * 
 * TODO: Implement a thread-safe lazy singleton.
 * 
 * ⚠️ CRITICAL: The volatile keyword is REQUIRED!
 * Without it, another thread might see a partially constructed object!
 * 
 * 💡 THINK: What goes wrong without volatile?
 * 
 *   The JVM may reorder instructions. Object construction involves:
 *   1. Allocate memory
 *   2. Initialize fields
 *   3. Assign reference to variable
 * 
 *   Without volatile, the order might be: 1 → 3 → 2
 *   Thread A: Allocates memory, assigns to INSTANCE (before init!)
 *   Thread B: Sees INSTANCE != null, returns partially constructed object!
 * 
 * 📝 NOTE: volatile prevents instruction reordering across the write.
 *   The assignment to INSTANCE "happens-after" all initialization.
 */
public class DoubleCheckedSingleton {
    
    // TODO: Add volatile keyword here!
    // ⚠️ AVOID: private static DoubleCheckedSingleton INSTANCE;
    // 🔑 HINT: private static volatile DoubleCheckedSingleton INSTANCE;
    private static volatile DoubleCheckedSingleton INSTANCE;
    
    private final String config;
    
    /**
     * Private constructor - prevents direct instantiation.
     */
    private DoubleCheckedSingleton() {
        // Simulate expensive initialization
        this.config = "Initialized at " + System.currentTimeMillis();
        
        // 💡 THINK: What if initialization takes 100ms?
        // Without proper synchronization, multiple instances could be created!
    }
    
    /**
     * TODO: Implement double-checked locking pattern.
     * 
     * 🔑 HINT - The correct pattern:
     * 
     *   if (INSTANCE == null) {           // First check (no lock)
     *       synchronized (DoubleCheckedSingleton.class) {
     *           if (INSTANCE == null) {   // Second check (with lock)
     *               INSTANCE = new DoubleCheckedSingleton();
     *           }
     *       }
     *   }
     *   return INSTANCE;
     * 
     * 💡 THINK: Why do we need TWO null checks?
     *   First check: Avoid synchronization overhead after initialization
     *   Second check: Another thread might have initialized while we waited for lock
     * 
     * @return the singleton instance
     */
    public static DoubleCheckedSingleton getInstance() {
        // TODO: Implement double-checked locking
        if (INSTANCE == null) {
            synchronized (DoubleCheckedSingleton.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DoubleCheckedSingleton();
                }
            }
        }
        return INSTANCE;
    }
    
    public String getConfig() {
        return config;
    }
    
    // For testing - reset the singleton (not for production!)
    static void resetForTesting() {
        INSTANCE = null;
    }
}
