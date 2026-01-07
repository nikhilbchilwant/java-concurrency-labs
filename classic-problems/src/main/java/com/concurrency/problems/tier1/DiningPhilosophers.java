package com.concurrency.problems.tier1;

/**
 * Classic Problem #3: Dining Philosophers
 * 
 * Five philosophers sit at a round table. Each needs TWO forks to eat.
 * Forks are placed between each pair of philosophers.
 * 
 * ⚠️ PROBLEM: If each philosopher picks up their left fork first,
 *   they all wait for the right fork → DEADLOCK!
 * 
 * TODO: Implement a solution that prevents deadlock.
 * 
 * 💡 SOLUTIONS (implement one or more):
 * 
 *   1. RESOURCE HIERARCHY (implemented here):
 *      Always pick up the lower-numbered fork first.
 *      Philosopher 4 picks up fork 0 before fork 4 (breaks the cycle!)
 * 
 *   2. WAITER (Semaphore):
 *      A "waiter" permits only 4 philosophers to try eating at once.
 *      (See DiningPhilosophersWaiter.java)
 * 
 *   3. ASYMMETRIC:
 *      Odd philosophers pick left first, even pick right first.
 * 
 * 📝 NOTE: This problem demonstrates the FOUR conditions for deadlock:
 *   1. Mutual Exclusion: Forks can't be shared
 *   2. Hold and Wait: Holding one fork while waiting for another
 *   3. No Preemption: Can't take a fork from another philosopher
 *   4. Circular Wait: A→B→C→D→E→A (broken by resource hierarchy!)
 */
public class DiningPhilosophers {
    
    private final int numPhilosophers;
    private final Object[] forks;
    
    public DiningPhilosophers(int numPhilosophers) {
        this.numPhilosophers = numPhilosophers;
        this.forks = new Object[numPhilosophers];
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Object();
        }
    }
    
    /**
     * TODO: Implement eating with deadlock prevention.
     * 
     * 🔑 HINT - Resource Hierarchy Solution:
     *   - Always acquire the lower-numbered fork first
     *   - This breaks the circular wait condition!
     * 
     * Example for Philosopher 2 (between forks 2 and 3):
     *   - First lock: fork[2] (lower number)
     *   - Second lock: fork[3] (higher number)
     * 
     * Example for Philosopher 4 (between forks 4 and 0):
     *   - First lock: fork[0] (lower number!)
     *   - Second lock: fork[4] (higher number)
     * 
     * 💡 THINK: Why does this prevent deadlock?
     *   All philosophers try to acquire a "lower" fork first.
     *   Someone will always succeed → No circular wait!
     */
    public void eat(int philosopherId) {
        int leftFork = philosopherId;
        int rightFork = (philosopherId + 1) % numPhilosophers;
        
        // TODO: Determine which fork to pick up first (lower numbered)
        int firstFork = Math.min(leftFork, rightFork);
        int secondFork = Math.max(leftFork, rightFork);
        
        // TODO: Acquire forks in order, eat, then release
        synchronized (forks[firstFork]) {
            synchronized (forks[secondFork]) {
                // Eating - in real code, do some work here
                doEat(philosopherId);
            }
        }
    }
    
    private void doEat(int philosopherId) {
        // Simulate eating
        System.out.println("Philosopher " + philosopherId + " is eating");
        try {
            Thread.sleep(10); // Simulate eating time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Creates and starts philosopher threads.
     * Each philosopher thinks, then eats, in a loop.
     */
    public void startDining(int rounds) {
        Thread[] philosophers = new Thread[numPhilosophers];
        
        for (int i = 0; i < numPhilosophers; i++) {
            final int id = i;
            philosophers[i] = new Thread(() -> {
                for (int r = 0; r < rounds; r++) {
                    think(id);
                    eat(id);
                }
            }, "Philosopher-" + i);
        }
        
        // Start all philosophers
        for (Thread t : philosophers) {
            t.start();
        }
        
        // Wait for all to complete
        for (Thread t : philosophers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void think(int philosopherId) {
        // Simulate thinking
        try {
            Thread.sleep((long) (Math.random() * 10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
