package com.concurrency.projects.pipeline;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Project 1: Producer-Consumer Pipeline
 * 
 * TODO: Build a multi-stage data processing pipeline.
 * 
 * 📝 NOTE: This integrates:
 *   - BlockingQueue for stage communication
 *   - Multiple producer/consumer threads
 *   - Graceful shutdown with poison pill
 *   - Backpressure handling
 * 
 * Real-world example: Log processing pipeline
 *   Stage 1: Read raw log lines (producer)
 *   Stage 2: Parse log entries
 *   Stage 3: Filter interesting events
 *   Stage 4: Write to database (consumer)
 */
public class DataPipeline<I, O> {
    
    private final BlockingQueue<I> inputQueue;
    private final BlockingQueue<O> outputQueue;
    private final Function<I, O> processor;
    private final int numWorkers;
    private final ExecutorService workers;
    private volatile boolean running = true;
    
    /**
     * Creates a pipeline stage.
     * 
     * @param inputQueue queue to read from
     * @param outputQueue queue to write to
     * @param processor transformation function
     * @param numWorkers number of parallel workers
     */
    public DataPipeline(BlockingQueue<I> inputQueue, 
                        BlockingQueue<O> outputQueue,
                        Function<I, O> processor,
                        int numWorkers) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.processor = processor;
        this.numWorkers = numWorkers;
        this.workers = Executors.newFixedThreadPool(numWorkers);
    }
    
    /**
     * TODO: Start the pipeline stage.
     * 
     * 🔑 HINT: Each worker should:
     *   1. Take from input queue
     *   2. Process the item
     *   3. Put result to output queue
     *   4. Handle shutdown signal (poison pill or interrupt)
     */
    public void start() {
        for (int i = 0; i < numWorkers; i++) {
            final int workerId = i;
            workers.submit(() -> {
                while (running) {
                    try {
                        // TODO: Take from input (with timeout for shutdown check)
                        I input = inputQueue.poll(100, TimeUnit.MILLISECONDS);
                        
                        if (input == null) {
                            continue; // Timeout, check running flag
                        }
                        
                        // TODO: Process and output
                        O output = processor.apply(input);
                        
                        if (output != null) {
                            outputQueue.put(output);
                        }
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        // Log and continue - don't let one bad item kill the worker
                        System.err.println("Worker " + workerId + " error: " + e.getMessage());
                    }
                }
                System.out.println("Worker " + workerId + " stopped");
            });
        }
    }
    
    /**
     * TODO: Stop the pipeline gracefully.
     */
    public void stop() {
        running = false;
        workers.shutdown();
        try {
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}

/**
 * Example: Complete log processing pipeline.
 * 
 * 💡 THINK: How would you:
 *   - Add monitoring (items processed per second)?
 *   - Handle backpressure (slow downstream stage)?
 *   - Implement exactly-once processing?
 */
class LogProcessingPipeline {
    
    public static void main(String[] args) throws InterruptedException {
        // Stage queues (bounded for backpressure!)
        BlockingQueue<String> rawLogs = new LinkedBlockingQueue<>(1000);
        BlockingQueue<LogEntry> parsedLogs = new LinkedBlockingQueue<>(1000);
        BlockingQueue<LogEntry> filteredLogs = new LinkedBlockingQueue<>(1000);
        
        // Stage 1: Raw -> Parsed
        DataPipeline<String, LogEntry> parser = new DataPipeline<>(
            rawLogs, parsedLogs,
            LogEntry::parse,
            2
        );
        
        // Stage 2: Parsed -> Filtered (only errors)
        DataPipeline<LogEntry, LogEntry> filter = new DataPipeline<>(
            parsedLogs, filteredLogs,
            entry -> entry.level.equals("ERROR") ? entry : null,
            1
        );
        
        // Start pipeline
        parser.start();
        filter.start();
        
        // Producer: feed raw logs
        Thread producer = new Thread(() -> {
            String[] sampleLogs = {
                "2024-01-07 10:00:00 INFO Starting application",
                "2024-01-07 10:00:01 ERROR Database connection failed",
                "2024-01-07 10:00:02 INFO Retrying...",
                "2024-01-07 10:00:03 ERROR Still failing",
                "2024-01-07 10:00:04 INFO Connected successfully"
            };
            
            try {
                for (String log : sampleLogs) {
                    rawLogs.put(log);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();
        
        // Consumer: print filtered logs
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    LogEntry entry = filteredLogs.poll(500, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        System.out.println("ALERT: " + entry);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();
        
        // Let it run
        Thread.sleep(2000);
        
        // Shutdown
        parser.stop();
        filter.stop();
        consumer.interrupt();
        
        System.out.println("Pipeline shutdown complete");
    }
    
    static class LogEntry {
        String timestamp;
        String level;
        String message;
        
        static LogEntry parse(String raw) {
            // Simple parsing
            String[] parts = raw.split(" ", 4);
            LogEntry entry = new LogEntry();
            entry.timestamp = parts[0] + " " + parts[1];
            entry.level = parts[2];
            entry.message = parts.length > 3 ? parts[3] : "";
            return entry;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", timestamp, level, message);
        }
    }
}
