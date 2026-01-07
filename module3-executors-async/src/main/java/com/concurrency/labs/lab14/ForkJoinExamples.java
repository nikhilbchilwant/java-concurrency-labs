package com.concurrency.labs.lab14;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Lab 14: Fork/Join Framework
 * 
 * TODO: Learn to parallelize divide-and-conquer algorithms.
 * 
 * 📝 NOTE: Fork/Join is designed for problems that can be broken into subtasks:
 *   1. If task is small enough → solve directly
 *   2. Otherwise → fork subtasks, join results
 * 
 * Key classes:
 *   - ForkJoinPool: Work-stealing thread pool
 *   - RecursiveTask<V>: Task that returns a result
 *   - RecursiveAction: Task with no result (void)
 * 
 * 💡 THINK: Work-stealing algorithm:
 *   Each thread has its own deque of tasks.
 *   When a thread's deque is empty, it "steals" from others.
 *   This balances load automatically!
 */
public class ForkJoinExamples {
    
    /**
     * TODO: Implement parallel sum using Fork/Join.
     * 
     * 🔑 HINT: The pattern is:
     *   if (size < THRESHOLD) {
     *       return computeDirectly();
     *   } else {
     *       SumTask left = new SumTask(arr, start, mid);
     *       SumTask right = new SumTask(arr, mid, end);
     *       left.fork();         // Queue the left task
     *       long rightResult = right.compute();  // Compute right in current thread
     *       long leftResult = left.join();       // Wait for left result
     *       return leftResult + rightResult;
     *   }
     * 
     * 📝 NOTE: Why compute one subtask directly instead of forking both?
     *   Forking has overhead. Computing one directly saves a fork.
     */
    public static class ParallelSum extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10_000;
        
        private final long[] array;
        private final int start;
        private final int end;
        
        public ParallelSum(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Long compute() {
            int length = end - start;
            
            // Base case: small enough to compute directly
            if (length <= THRESHOLD) {
                return computeSequentially();
            }
            
            // Recursive case: split and conquer
            int mid = start + length / 2;
            
            // TODO: Fork left subtask
            ParallelSum leftTask = new ParallelSum(array, start, mid);
            leftTask.fork();  // Queue for parallel execution
            
            // TODO: Compute right subtask in current thread
            ParallelSum rightTask = new ParallelSum(array, mid, end);
            long rightResult = rightTask.compute();  // Direct computation
            
            // TODO: Join left result
            long leftResult = leftTask.join();  // Wait for forked task
            
            return leftResult + rightResult;
        }
        
        private long computeSequentially() {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        }
    }
    
    /**
     * TODO: Implement parallel merge sort.
     * 
     * 📝 NOTE: Merge sort is naturally divide-and-conquer:
     *   1. Split array in half
     *   2. Sort each half (can be parallel!)
     *   3. Merge sorted halves
     */
    public static class ParallelMergeSort extends RecursiveTask<int[]> {
        private static final int THRESHOLD = 1000;
        
        private final int[] array;
        
        public ParallelMergeSort(int[] array) {
            this.array = array;
        }
        
        @Override
        protected int[] compute() {
            if (array.length <= THRESHOLD) {
                // Base case: use sequential sort
                return sequentialSort(array);
            }
            
            int mid = array.length / 2;
            int[] left = new int[mid];
            int[] right = new int[array.length - mid];
            
            System.arraycopy(array, 0, left, 0, mid);
            System.arraycopy(array, mid, right, 0, array.length - mid);
            
            // TODO: Fork left, compute right, join left, merge
            ParallelMergeSort leftTask = new ParallelMergeSort(left);
            ParallelMergeSort rightTask = new ParallelMergeSort(right);
            
            leftTask.fork();
            int[] rightSorted = rightTask.compute();
            int[] leftSorted = leftTask.join();
            
            return merge(leftSorted, rightSorted);
        }
        
        private int[] sequentialSort(int[] arr) {
            // Simple insertion sort for small arrays
            for (int i = 1; i < arr.length; i++) {
                int key = arr[i];
                int j = i - 1;
                while (j >= 0 && arr[j] > key) {
                    arr[j + 1] = arr[j];
                    j--;
                }
                arr[j + 1] = key;
            }
            return arr;
        }
        
        private int[] merge(int[] left, int[] right) {
            int[] result = new int[left.length + right.length];
            int i = 0, j = 0, k = 0;
            
            while (i < left.length && j < right.length) {
                if (left[i] <= right[j]) {
                    result[k++] = left[i++];
                } else {
                    result[k++] = right[j++];
                }
            }
            
            while (i < left.length) result[k++] = left[i++];
            while (j < right.length) result[k++] = right[j++];
            
            return result;
        }
    }
    
    /**
     * ⚠️ AVOID: Common Fork/Join mistakes.
     */
    public static void commonMistakes() {
        // ⚠️ WRONG: Forking both subtasks
        // leftTask.fork();
        // rightTask.fork();
        // leftTask.join() + rightTask.join();
        // This creates unnecessary overhead!
        
        // ⚠️ WRONG: Joining before forking completes
        // leftTask.fork();
        // leftTask.join();  // Wait immediately - no parallelism!
        // rightTask.compute();
        
        // ⚠️ WRONG: Threshold too small
        // if (size < 10) // Too much overhead for tiny gains
    }
    
    public static void main(String[] args) {
        // Create test array
        int size = 10_000_000;
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        
        // Use common pool
        ForkJoinPool pool = ForkJoinPool.commonPool();
        
        long start = System.currentTimeMillis();
        long result = pool.invoke(new ParallelSum(array, 0, size));
        long end = System.currentTimeMillis();
        
        System.out.println("Sum: " + result);
        System.out.println("Time: " + (end - start) + " ms");
        System.out.println("Expected: " + ((long) size * (size - 1) / 2));
    }
}
