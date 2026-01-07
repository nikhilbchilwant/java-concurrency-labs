package com.concurrency.labs.lab19;

/**
 * Lab 19: Race Detection with JCStress
 * 
 * 📝 NOTE: JCStress is OpenJDK's concurrency stress testing tool.
 * It's the gold standard for proving race conditions exist.
 * 
 * JCStress works by:
 *   1. Running many parallel iterations
 *   2. Observing all possible outcomes
 *   3. Categorizing outcomes (expected vs unexpected)
 * 
 * ⚠️ This is a conceptual example - JCStress requires its own build setup.
 * See: https://github.com/openjdk/jcstress
 * 
 * 💡 THINK: Why is JCStress better than hand-written stress tests?
 *   - Runs millions of iterations across thread combinations
 *   - Uses memory barriers to expose weak memory model effects
 *   - Categorizes outcomes systematically
 */
public class RaceDetectionConcepts {
    
    /**
     * Example of a race condition we want to prove.
     * 
     * 📝 NOTE: This shows the concept. Real JCStress uses annotations:
     * 
     * @JCStressTest
     * @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Both saw each other's write")
     * @Outcome(id = "0, 0", expect = ACCEPTABLE_INTERESTING, desc = "Neither saw the other's write - visibility issue!")
     * @State
     * public class VisibilityTest {
     *     int x, y;
     *     
     *     @Actor
     *     public void actor1(II_Result r) {
     *         x = 1;
     *         r.r1 = y;
     *     }
     *     
     *     @Actor
     *     public void actor2(II_Result r) {
     *         y = 1;
     *         r.r2 = x;
     *     }
     * }
     * 
     * Result (0, 0) proves visibility bug - both writes not visible!
     */
    public static void jcstressConceptExample() {
        System.out.println("JCStress Concept: Visibility Race");
        System.out.println();
        System.out.println("Without proper memory synchronization:");
        System.out.println("  Thread A: x = 1; read y;");
        System.out.println("  Thread B: y = 1; read x;");
        System.out.println();
        System.out.println("Possible outcomes:");
        System.out.println("  (1, 1) - Both saw each other's write ✓");
        System.out.println("  (1, 0) - A wrote first, B saw x but A didn't see y ✓");
        System.out.println("  (0, 1) - B wrote first, A saw y but B didn't see x ✓");
        System.out.println("  (0, 0) - Neither saw the other's write! ⚠️ RACE!");
        System.out.println();
        System.out.println("Regular tests might never see (0, 0).");
        System.out.println("JCStress WILL find it if it's possible!");
    }
    
    /**
     * Simple demonstration of proving a race exists.
     * 
     * Run this many times - you should see inconsistent results.
     */
    public static void proveRaceCondition() {
        int races = 0;
        int trials = 1000;
        
        for (int trial = 0; trial < trials; trial++) {
            final int[] x = {0};
            final int[] y = {0};
            final int[] r1 = {0};
            final int[] r2 = {0};
            
            Thread t1 = new Thread(() -> {
                x[0] = 1;
                r1[0] = y[0];
            });
            
            Thread t2 = new Thread(() -> {
                y[0] = 1;
                r2[0] = x[0];
            });
            
            t1.start();
            t2.start();
            
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // The (0, 0) outcome proves a visibility race!
            if (r1[0] == 0 && r2[0] == 0) {
                races++;
            }
        }
        
        System.out.println("Ran " + trials + " trials");
        System.out.println("Detected race condition (0,0) in " + races + " trials");
        
        if (races > 0) {
            System.out.println("RACE CONDITION PROVEN! ⚠️");
        } else {
            System.out.println("Race not observed (doesn't mean it doesn't exist!)");
            System.out.println("Use JCStress for definitive proof.");
        }
    }
    
    public static void main(String[] args) {
        jcstressConceptExample();
        System.out.println("=".repeat(50));
        proveRaceCondition();
    }
}
