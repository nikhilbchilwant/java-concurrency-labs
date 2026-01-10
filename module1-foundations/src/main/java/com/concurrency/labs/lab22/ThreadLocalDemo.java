package com.concurrency.labs.lab22;

/**
 * Lab 22: ThreadLocal - Thread Confinement Pattern
 * 
 * 📝 NOTE: ThreadLocal is a way to AVOID synchronization entirely!
 *   Each thread gets its own copy of the variable.
 * 
 * Use cases:
 *   - Per-request context (user session, transaction ID)
 *   - Thread-unsafe objects (SimpleDateFormat, Random)
 *   - Database connections in thread pools
 * 
 * 💡 THINK: Why ThreadLocal instead of synchronizing?
 *   - No contention = better performance
 *   - Simpler code = fewer bugs
 *   - But: uses more memory (one copy per thread)
 * 
 * ⚠️ AVOID: Memory leaks!
 *   In thread pools, threads are reused. If you don't call .remove(),
 *   the value persists and can leak to the next request!
 */
public class ThreadLocalDemo {
    
    // ========== BASIC USAGE ==========
    
    /**
     * Simple ThreadLocal with initial value.
     */
    private static final ThreadLocal<Integer> threadId = 
        ThreadLocal.withInitial(() -> -1);
    
    /**
     * ThreadLocal for request context.
     * 
     * 📝 NOTE: Common interview pattern - "How would you pass user context
     *   through multiple service layers without passing it as a parameter?"
     */
    private static final ThreadLocal<RequestContext> requestContext = 
        new ThreadLocal<>();
    
    public static class RequestContext {
        private final String userId;
        private final String traceId;
        private final long startTime;
        
        public RequestContext(String userId, String traceId) {
            this.userId = userId;
            this.traceId = traceId;
            this.startTime = System.currentTimeMillis();
        }
        
        public String getUserId() { return userId; }
        public String getTraceId() { return traceId; }
        public long getElapsedMs() { return System.currentTimeMillis() - startTime; }
        
        @Override
        public String toString() {
            return String.format("[user=%s, trace=%s]", userId, traceId);
        }
    }
    
    // ========== PROPER USAGE PATTERN ==========
    
    /**
     * TODO: Set context at request start, clean up at end.
     * 
     * 🔑 HINT: ALWAYS use try-finally to ensure cleanup!
     */
    public static void handleRequest(String userId, String traceId) {
        // Set context for this thread
        requestContext.set(new RequestContext(userId, traceId));
        
        try {
            // Business logic - context is available anywhere in this thread
            processOrder();
            sendNotification();
            
            // Log elapsed time
            System.out.printf("Request %s completed in %dms%n",
                requestContext.get().getTraceId(),
                requestContext.get().getElapsedMs());
                
        } finally {
            // ⚠️ CRITICAL: Always remove to prevent memory leaks!
            requestContext.remove();
        }
    }
    
    private static void processOrder() {
        // Can access context without parameter passing
        RequestContext ctx = requestContext.get();
        System.out.println("Processing order for user: " + ctx.getUserId());
    }
    
    private static void sendNotification() {
        RequestContext ctx = requestContext.get();
        System.out.println("Sending notification, trace: " + ctx.getTraceId());
    }
    
    // ========== THREAD-UNSAFE OBJECTS ==========
    
    /**
     * 📝 NOTE: SimpleDateFormat is NOT thread-safe!
     *   Options:
     *   1. Create new instance each time (expensive)
     *   2. Synchronize access (contention)
     *   3. Use ThreadLocal (best for heavy usage)
     */
    private static final ThreadLocal<java.text.SimpleDateFormat> dateFormat =
        ThreadLocal.withInitial(() -> 
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    
    public static String formatDate(java.util.Date date) {
        // Each thread gets its own formatter - no synchronization needed!
        return dateFormat.get().format(date);
    }
    
    // ========== INHERITABLE THREAD LOCAL ==========
    
    /**
     * InheritableThreadLocal: Child threads inherit parent's value.
     * 
     * 💡 THINK: When would you use this?
     *   - Passing context to spawned threads
     *   - But careful: value is copied at thread creation time!
     */
    private static final InheritableThreadLocal<String> inheritableContext =
        new InheritableThreadLocal<>();
    
    // ========== MEMORY LEAK DEMO ==========
    
    /**
     * ⚠️ AVOID: This causes memory leaks in thread pools!
     */
    public static void badPattern() {
        // In a thread pool, this thread will be reused
        requestContext.set(new RequestContext("user123", "trace-abc"));
        
        // Process request...
        
        // FORGOT TO CALL requestContext.remove() !!!
        // The context persists and may leak to next request!
    }
    
    // ========== DEMO ==========
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ThreadLocal Demo ===\n");
        
        // Demo 1: Each thread has its own value
        System.out.println("--- Per-thread values ---");
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                threadId.set(id);
                System.out.println(Thread.currentThread().getName() + 
                    " has threadId: " + threadId.get());
            }, "Worker-" + i);
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        
        // Demo 2: Request context pattern
        System.out.println("\n--- Request context pattern ---");
        Thread req1 = new Thread(() -> handleRequest("alice", "trace-001"));
        Thread req2 = new Thread(() -> handleRequest("bob", "trace-002"));
        req1.start();
        req2.start();
        req1.join();
        req2.join();
        
        // Demo 3: Thread-safe date formatting
        System.out.println("\n--- Thread-safe date formatting ---");
        java.util.Date now = new java.util.Date();
        System.out.println("Formatted: " + formatDate(now));
        
        System.out.println("\n✅ ThreadLocal lets you avoid synchronization by giving each thread its own copy!");
    }
}
