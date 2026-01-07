package com.concurrency.problems.tier3;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classic Problem #6: Thread-Safe LRU Cache
 * 
 * TODO: Implement an LRU cache that is thread-safe AND performant.
 * 
 * ⚠️ AVOID: Wrapping LinkedHashMap with synchronized!
 *   
 *   // BAD - This kills ALL concurrency!
 *   Collections.synchronizedMap(new LinkedHashMap<>(capacity, 0.75f, true) {
 *       @Override protected boolean removeEldestEntry(Map.Entry eldest) {
 *           return size() > capacity;
 *       }
 *   });
 *   
 *   Why is this bad? Every get() AND every put() acquires the same lock!
 *   Readers block readers. Terrible for read-heavy workloads.
 * 
 * 💡 THINK: Better approach using two data structures:
 *   1. ConcurrentHashMap for O(1) thread-safe key-value lookup
 *   2. Custom Doubly Linked List for LRU ordering (with fine-grained locking)
 * 
 * 📝 NOTE: On every access:
 *   - Move the accessed node to the head (most recently used)
 *   - On eviction, remove from tail (least recently used)
 * 
 * @param <K> key type
 * @param <V> value type
 */
public class ConcurrentLRUCache<K, V> {
    
    /**
     * Doubly-linked list node.
     * 
     * 📝 NOTE: We store the key so we can remove from ConcurrentHashMap on eviction.
     */
    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private final int capacity;
    
    // TODO: Use ConcurrentHashMap for thread-safe key-to-node mapping
    // 🔑 HINT: ConcurrentHashMap<K, Node<K, V>>
    private final ConcurrentHashMap<K, Node<K, V>> cache;
    
    // Doubly-linked list: head = most recent, tail = least recent
    private final Node<K, V> head; // Sentinel node
    private final Node<K, V> tail; // Sentinel node
    
    // TODO: Fine-grained lock for the linked list operations
    // 💡 THINK: Why a separate lock instead of synchronizing on 'this'?
    //   The CHM handles its own synchronization - we only need to protect the list!
    private final Lock listLock = new ReentrantLock();
    
    public ConcurrentLRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.cache = new ConcurrentHashMap<>();
        
        // Initialize sentinel nodes
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }
    
    /**
     * TODO: Get a value from the cache.
     * 
     * 🔑 HINT:
     *   1. Look up node in ConcurrentHashMap (no lock needed!)
     *   2. If found, move to head of list (lock needed)
     *   3. Return value
     * 
     * 📝 NOTE: ConcurrentHashMap.get() is thread-safe and non-blocking!
     * We only need the lock for list manipulation.
     * 
     * @param key the key to look up
     * @return the value, or null if not found
     */
    public V get(K key) {
        Node<K, V> node = cache.get(key);
        if (node == null) {
            return null;
        }
        
        // TODO: Move node to head (most recently used)
        // 🔑 HINT: moveToHead(node) with listLock held
        listLock.lock();
        try {
            moveToHead(node);
        } finally {
            listLock.unlock();
        }
        
        return node.value;
    }
    
    /**
     * TODO: Put a value into the cache.
     * 
     * 🔑 HINT:
     *   1. Check if key exists in cache
     *   2. If exists: update value, move to head
     *   3. If new: create node, add to head, evict if over capacity
     * 
     * ⚠️ AVOID: Race condition between check and insert!
     *   Use ConcurrentHashMap.compute() or computeIfAbsent() for atomicity.
     * 
     * @param key the key
     * @param value the value
     */
    public void put(K key, V value) {
        // TODO: Implement thread-safe put with eviction
        
        // 💡 THINK: There's a subtle race condition here.
        // What if two threads try to put the same key simultaneously?
        // What if size check and eviction are not atomic?
        
        // Option 1: Use cache.compute() for atomic check-and-update
        // Option 2: Hold listLock for entire operation (simpler but less concurrent)
        
        listLock.lock();
        try {
            Node<K, V> existing = cache.get(key);
            if (existing != null) {
                // Update existing node
                existing.value = value;
                moveToHead(existing);
            } else {
                // Create new node
                Node<K, V> newNode = new Node<>(key, value);
                cache.put(key, newNode);
                addToHead(newNode);
                
                // Evict if over capacity
                if (cache.size() > capacity) {
                    Node<K, V> lru = removeTail();
                    if (lru != null) {
                        cache.remove(lru.key);
                    }
                }
            }
        } finally {
            listLock.unlock();
        }
    }
    
    // === Helper methods for doubly-linked list ===
    
    /**
     * Adds a node right after head (most recently used position).
     * Must be called with listLock held.
     */
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    /**
     * Removes a node from its current position in the list.
     * Must be called with listLock held.
     */
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    /**
     * Moves an existing node to the head (most recently used).
     * Must be called with listLock held.
     */
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }
    
    /**
     * Removes and returns the tail node (least recently used).
     * Must be called with listLock held.
     * 
     * @return the removed node, or null if list is empty
     */
    private Node<K, V> removeTail() {
        Node<K, V> lru = tail.prev;
        if (lru == head) {
            return null; // List is empty
        }
        removeNode(lru);
        return lru;
    }
    
    public int size() {
        return cache.size();
    }
}
