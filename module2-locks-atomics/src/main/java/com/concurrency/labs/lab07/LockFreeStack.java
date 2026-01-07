package com.concurrency.labs.lab07;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Lab 07: Compare-And-Swap (CAS) Operations
 * 
 * TODO: Implement a lock-free stack using CAS.
 * 
 * 📝 NOTE: CAS is the foundation of lock-free data structures!
 *   compareAndSet(expected, new):
 *     - If current value == expected, set to new and return true
 *     - Otherwise, return false (another thread modified it)
 * 
 * 💡 THINK: Why is CAS better than locking in high-contention scenarios?
 *   - No thread ever blocks waiting for a lock
 *   - Threads that "lose" the CAS just retry immediately
 *   - No deadlock possible!
 * 
 * ⚠️ AVOID: The ABA problem!
 *   - Thread 1 reads A, gets interrupted
 *   - Thread 2 changes A→B→A
 *   - Thread 1 resumes, CAS succeeds (sees A), but state has changed!
 *   - Solution: Use AtomicStampedReference to include a version number
 * 
 * @param <E> element type
 */
public class LockFreeStack<E> {
    
    /**
     * Node in the stack - immutable once created.
     */
    private static class Node<E> {
        final E value;
        final Node<E> next;
        
        Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }
    
    // TODO: Use AtomicReference for the head pointer
    // 🔑 HINT: AtomicReference<Node<E>> top = new AtomicReference<>();
    private final AtomicReference<Node<E>> top = new AtomicReference<>();
    
    /**
     * TODO: Push an element onto the stack using CAS.
     * 
     * 🔑 HINT: The pattern is:
     *   while (true) {
     *       Node oldHead = top.get();
     *       Node newHead = new Node(value, oldHead);
     *       if (top.compareAndSet(oldHead, newHead)) {
     *           return; // success!
     *       }
     *       // CAS failed, another thread modified - retry
     *   }
     */
    public void push(E value) {
        // TODO: Implement CAS-based push
        // 📝 NOTE: This loop will eventually succeed - guaranteed progress!
    }
    
    /**
     * TODO: Pop an element from the stack using CAS.
     * 
     * 💡 THINK: What should happen if the stack is empty?
     *   - Return null? Throw exception? Block until not empty?
     *   - This is a design decision - document it!
     * 
     * @return the popped value, or null if empty
     */
    public E pop() {
        // TODO: Implement CAS-based pop
        // 🔑 HINT: Similar pattern to push
        return null;
    }
    
    /**
     * Peek at the top element without removing it.
     * 
     * 📝 NOTE: This is naturally thread-safe - just a single read!
     */
    public E peek() {
        Node<E> head = top.get();
        return head != null ? head.value : null;
    }
    
    public boolean isEmpty() {
        return top.get() == null;
    }
}
