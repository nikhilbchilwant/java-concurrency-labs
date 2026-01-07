package com.concurrency.labs.lab09;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lab 09: ConcurrentHashMap Internals
 * 
 * TODO: Learn to use ConcurrentHashMap correctly.
 * 
 * 📝 NOTE: ConcurrentHashMap is NOT just a synchronized HashMap!
 *   It uses fine-grained locking (lock striping) for high concurrency.
 * 
 * Evolution of CHM locking:
 *   Java 7: Segment-based (16 segments = 16 concurrent writers)
 *   Java 8+: Node-based with CAS + synchronized on individual bins
 * 
 * 💡 THINK: Why is CHM faster than Collections.synchronizedMap()?
 *   synchronized map: Lock entire map for every operation
 *   CHM: Lock only the specific bucket being modified
 */
public class ConcurrentHashMapUsage {
    
    /**
     * TODO: Demonstrate the race condition in check-then-act.
     * 
     * ⚠️ AVOID: This pattern is broken even with ConcurrentHashMap!
     * 
     *   if (!map.containsKey(key)) {   // Check
     *       map.put(key, value);        // Act
     *   }
     * 
     * Problem: Another thread can put between check and act!
     */
    public static void brokenCheckThenAct(ConcurrentHashMap<String, Integer> map, 
                                          String key, Integer value) {
        // ⚠️ BROKEN! Race condition between containsKey and put
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
        // 💡 THINK: What if two threads both see containsKey return false?
    }
    
    /**
     * TODO: Use atomic operations to fix the race condition.
     * 
     * 🔑 HINT: ConcurrentHashMap provides atomic compound operations:
     *   - putIfAbsent(key, value): Only puts if key doesn't exist
     *   - computeIfAbsent(key, mappingFunction): Computes if absent
     *   - compute(key, remappingFunction): Always computes new value
     *   - merge(key, value, remappingFunction): Merges with existing
     */
    public static void correctPutIfAbsent(ConcurrentHashMap<String, Integer> map, 
                                          String key, Integer value) {
        // ✅ Atomic operation - no race condition!
        map.putIfAbsent(key, value);
    }
    
    /**
     * TODO: Use computeIfAbsent for lazy initialization.
     * 
     * 📝 NOTE: computeIfAbsent is perfect for caching!
     *   map.computeIfAbsent(key, k -> expensiveComputation(k));
     * 
     * The computation runs only if key is absent, AND it's atomic!
     */
    public static Integer getOrCompute(ConcurrentHashMap<String, Integer> map, 
                                       String key) {
        // 🔑 HINT: The lambda only runs if key is absent
        return map.computeIfAbsent(key, k -> {
            System.out.println("Computing value for: " + k);
            return k.length(); // Expensive computation would go here
        });
    }
    
    /**
     * TODO: Use compute for atomic read-modify-write.
     * 
     * Problem: How to atomically increment a value in the map?
     * 
     * ⚠️ AVOID: This is broken!
     *   Integer old = map.get(key);
     *   map.put(key, old + 1);
     */
    public static void atomicIncrement(ConcurrentHashMap<String, Integer> map, 
                                       String key) {
        // ✅ Atomic increment using compute
        map.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
        
        // Alternative using merge:
        // map.merge(key, 1, Integer::sum);
    }
    
    /**
     * TODO: Use merge for aggregation.
     * 
     * 📝 NOTE: merge is perfect for counters and aggregations!
     *   merge(key, value, remappingFunction)
     *   - If absent: put(key, value)
     *   - If present: put(key, remappingFunction(oldValue, value))
     */
    public static void wordCount(ConcurrentHashMap<String, Integer> wordCounts, 
                                 String word) {
        // 🔑 HINT: Merge adds 1, or adds to existing count
        wordCounts.merge(word, 1, Integer::sum);
        
        // 💡 THINK: How is this different from compute?
        // - merge: Combines new value with old value
        // - compute: Creates new value based on key and old value
    }
    
    /**
     * Demonstrates weakly consistent iteration.
     * 
     * 📝 NOTE: CHM iterators are "weakly consistent":
     *   - Reflect state at some point during or since iterator creation
     *   - Never throw ConcurrentModificationException
     *   - May or may not reflect concurrent modifications
     * 
     * 💡 THINK: Why is this usually acceptable?
     *   For most use cases, we just need a snapshot-ish view.
     *   Strong consistency would require full locking (slow!).
     */
    public static void demonstrateWeakConsistency() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        
        // Start iterating
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
            
            // Concurrent modification is safe (no exception!)
            map.put("c", 3);
        }
        
        // 💡 THINK: Will "c" be seen by the iterator?
        // Answer: Maybe! It's weakly consistent.
    }
    
    /**
     * ⚠️ AVOID: Using size() or isEmpty() for coordination!
     * 
     * These methods are NOT atomic with respect to modifications.
     * By the time you use the result, it might be stale!
     */
    public static void sizeIsNotReliable(ConcurrentHashMap<String, Integer> map) {
        // ⚠️ This is broken for coordination!
        if (map.size() == 0) {
            // Another thread might have added something!
        }
        
        // For coordination, use atomic operations like computeIfAbsent
    }
}
