package com.concurrency.projects.orchestrator;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Project 2: Async Job Orchestrator
 * 
 * TODO: Build a job orchestrator that handles dependencies between tasks.
 * 
 * 📝 NOTE: This integrates:
 *   - CompletableFuture for async composition
 *   - Dependency resolution (DAG execution)
 *   - Timeout handling
 *   - Error propagation
 * 
 * Real-world example: Build system, workflow engines, data pipelines
 * 
 * 💡 THINK: A job can only run when all its dependencies complete.
 *   Job C depends on A and B → C starts only after both A and B finish.
 */
public class JobOrchestrator {
    
    private final ExecutorService executor;
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Object>> futures = new ConcurrentHashMap<>();
    
    public JobOrchestrator(int parallelism) {
        this.executor = Executors.newFixedThreadPool(parallelism);
    }
    
    /**
     * A job with dependencies.
     */
    public static class Job {
        final String id;
        final Supplier<Object> task;
        final Set<String> dependencies;
        
        public Job(String id, Supplier<Object> task, String... dependencies) {
            this.id = id;
            this.task = task;
            this.dependencies = new HashSet<>(Arrays.asList(dependencies));
        }
    }
    
    /**
     * TODO: Register a job.
     */
    public void addJob(Job job) {
        jobs.put(job.id, job);
    }
    
    /**
     * TODO: Execute all jobs respecting dependencies.
     * 
     * 🔑 HINT: For each job:
     *   1. Get futures of all dependencies
     *   2. Use CompletableFuture.allOf() to wait for all
     *   3. Then execute this job
     * 
     * @return future that completes when all jobs are done
     */
    public CompletableFuture<Void> executeAll() {
        // Create futures for all jobs
        for (Job job : jobs.values()) {
            getOrCreateFuture(job.id);
        }
        
        // Return future that completes when all jobs are done
        return CompletableFuture.allOf(
            futures.values().toArray(new CompletableFuture[0])
        );
    }
    
    /**
     * TODO: Get or create the future for a job.
     * 
     * 📝 NOTE: This is where the magic happens!
     *   - If job has no dependencies, run immediately
     *   - If job has dependencies, wait for them first
     */
    @SuppressWarnings("unchecked")
    private CompletableFuture<Object> getOrCreateFuture(String jobId) {
        return futures.computeIfAbsent(jobId, id -> {
            Job job = jobs.get(id);
            if (job == null) {
                return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Unknown job: " + id));
            }
            
            if (job.dependencies.isEmpty()) {
                // No dependencies - run immediately
                return CompletableFuture.supplyAsync(job.task, executor);
            } else {
                // Wait for all dependencies first
                CompletableFuture<Object>[] deps = job.dependencies.stream()
                    .map(this::getOrCreateFuture)
                    .toArray(CompletableFuture[]::new);
                
                return CompletableFuture.allOf(deps)
                    .thenApplyAsync(v -> {
                        System.out.println("Starting job: " + job.id);
                        return job.task.get();
                    }, executor);
            }
        });
    }
    
    /**
     * Get the result of a specific job.
     */
    public Object getResult(String jobId) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = futures.get(jobId);
        if (future == null) {
            throw new IllegalArgumentException("Job not executed: " + jobId);
        }
        return future.get();
    }
    
    /**
     * Shutdown the orchestrator.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Example: Build system simulation.
     * 
     *     A       B
     *      \     /
     *        C
     *        |
     *        D
     * 
     * A and B can run in parallel.
     * C waits for both A and B.
     * D waits for C.
     */
    public static void main(String[] args) throws Exception {
        JobOrchestrator orchestrator = new JobOrchestrator(4);
        
        // Define jobs
        orchestrator.addJob(new Job("compile-module-a", () -> {
            sleep(500);
            System.out.println("Compiled module A");
            return "A.class";
        }));
        
        orchestrator.addJob(new Job("compile-module-b", () -> {
            sleep(300);
            System.out.println("Compiled module B");
            return "B.class";
        }));
        
        orchestrator.addJob(new Job("link", () -> {
            sleep(200);
            System.out.println("Linked A + B");
            return "app.exe";
        }, "compile-module-a", "compile-module-b")); // Depends on A and B
        
        orchestrator.addJob(new Job("package", () -> {
            sleep(100);
            System.out.println("Packaged");
            return "app.zip";
        }, "link")); // Depends on link
        
        System.out.println("Starting build...");
        long start = System.currentTimeMillis();
        
        // Execute all
        orchestrator.executeAll().join();
        
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Build complete in " + elapsed + "ms");
        System.out.println("Result: " + orchestrator.getResult("package"));
        
        // 💡 THINK: Total time should be ~800ms, not 1100ms
        // Because A and B run in parallel!
        
        orchestrator.shutdown();
    }
    
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
