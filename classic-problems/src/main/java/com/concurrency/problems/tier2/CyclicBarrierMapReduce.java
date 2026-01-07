package com.concurrency.problems.tier2;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Classic Problem #5: Cyclic Barrier - MapReduce Simulation
 * 
 * TODO: Use CyclicBarrier to coordinate phases of computation.
 * 
 * 📝 NOTE: CyclicBarrier vs CountDownLatch:
 *   - CountDownLatch: One-time use, threads count down
 *   - CyclicBarrier: Reusable, threads wait for each other
 * 
 * Scenario: MapReduce-style computation
 *   Phase 1 (Map): Each worker processes its partition
 *   Barrier: All workers wait until everyone finishes Map
 *   Phase 2 (Reduce): Results are aggregated
 * 
 * 💡 THINK: Why CyclicBarrier for MapReduce?
 *   - All mappers must finish before reduce can start
 *   - The barrier ensures synchronization point
 *   - "Cyclic" means we can reuse it for multiple rounds
 */
public class CyclicBarrierMapReduce {
    
    private final int numWorkers;
    private final int[] data;
    private final int[] partialSums;
    private final CyclicBarrier barrier;
    
    /**
     * Creates a MapReduce simulation.
     * 
     * @param numWorkers number of parallel workers
     * @param data input data (will be partitioned among workers)
     */
    public CyclicBarrierMapReduce(int numWorkers, int[] data) {
        this.numWorkers = numWorkers;
        this.data = data;
        this.partialSums = new int[numWorkers];
        
        // TODO: Create CyclicBarrier with numWorkers parties
        // 🔑 HINT: The second argument is the barrier action - runs when all arrive
        this.barrier = new CyclicBarrier(numWorkers, () -> {
            // This runs when all workers reach the barrier
            // 📝 NOTE: Only ONE thread runs this (last to arrive)
            System.out.println("All workers reached barrier - starting reduce phase");
        });
    }
    
    /**
     * Worker that processes its partition and waits at barrier.
     */
    public class MapWorker implements Runnable {
        private final int workerId;
        private final int start;
        private final int end;
        
        public MapWorker(int workerId, int start, int end) {
            this.workerId = workerId;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public void run() {
            try {
                // ========== MAP PHASE ==========
                System.out.println("Worker " + workerId + " starting map phase");
                
                // Process partition
                int sum = 0;
                for (int i = start; i < end; i++) {
                    sum += data[i];
                    // Simulate some processing time
                    Thread.sleep(10);
                }
                partialSums[workerId] = sum;
                
                System.out.println("Worker " + workerId + " finished map, sum=" + sum);
                
                // ========== BARRIER ==========
                // TODO: Wait for all workers to finish map phase
                // 🔑 HINT: barrier.await() blocks until all parties arrive
                barrier.await();
                
                // ========== REDUCE PHASE ==========
                // Only worker 0 does the reduce
                if (workerId == 0) {
                    int total = 0;
                    for (int partial : partialSums) {
                        total += partial;
                    }
                    System.out.println("Reduce complete: total = " + total);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                // 📝 NOTE: This happens if any thread is interrupted
                // or if barrier is reset while threads are waiting
                System.err.println("Barrier broken: " + e.getMessage());
            }
        }
    }
    
    /**
     * Run the MapReduce computation.
     */
    public void run() throws InterruptedException {
        Thread[] workers = new Thread[numWorkers];
        int partitionSize = data.length / numWorkers;
        
        for (int i = 0; i < numWorkers; i++) {
            int start = i * partitionSize;
            int end = (i == numWorkers - 1) ? data.length : start + partitionSize;
            
            workers[i] = new Thread(new MapWorker(i, start, end), "Worker-" + i);
            workers[i].start();
        }
        
        for (Thread worker : workers) {
            worker.join();
        }
    }
    
    /**
     * TODO: Demonstrate reusable nature of CyclicBarrier.
     * 
     * 💡 THINK: Unlike CountDownLatch, CyclicBarrier can be reused!
     * After all threads pass the barrier, it resets automatically.
     */
    public void runMultipleRounds(int rounds) throws InterruptedException {
        for (int round = 0; round < rounds; round++) {
            System.out.println("\n=== Round " + (round + 1) + " ===");
            run();
            // Barrier resets automatically for next round!
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        // Create test data
        int[] data = new int[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = i + 1;
        }
        
        CyclicBarrierMapReduce mr = new CyclicBarrierMapReduce(4, data);
        mr.run();
        
        // Expected: 1+2+...+100 = 5050
    }
}
