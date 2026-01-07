package com.concurrency.problems.tier4;

/**
 * Enum Singleton - The RECOMMENDED approach!
 * 
 * 📝 NOTE: This is Joshua Bloch's recommended singleton pattern (Effective Java).
 * 
 * Why is Enum singleton best?
 *   1. Thread-safe by JVM guarantee (enum initialization is synchronized)
 *   2. Serialization-safe (JVM handles it)
 *   3. Reflection-safe (can't create new enum instances via reflection)
 *   4. Simple and concise!
 * 
 * ⚠️ AVOID: Don't use enum singleton when:
 *   - You need lazy initialization with arguments
 *   - You need to extend a class (enums can't extend)
 *   - You need multiple instances later (hard to refactor)
 * 
 * 💡 THINK: Compare this to DoubleCheckedSingleton.
 *   Which is easier to get right? Which is easier to understand?
 */
public enum EnumSingleton {
    
    INSTANCE;
    
    // Instance fields
    private final String config;
    
    // Private constructor (implicit for enums)
    EnumSingleton() {
        this.config = "Initialized at " + System.currentTimeMillis();
    }
    
    public String getConfig() {
        return config;
    }
    
    /**
     * Example method on the singleton.
     */
    public void doSomething() {
        System.out.println("Singleton doing something with config: " + config);
    }
}
