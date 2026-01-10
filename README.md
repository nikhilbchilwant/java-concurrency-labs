# Java Concurrency Labs - SDE2 Interview Prep

> **AI-Generated Repository**: This project was created with AI assistance.

A hands-on learning project with skeleton code for mastering Java concurrency.

## Prerequisites

- Java 17+
- Maven 3.8+

## Quick Start

```bash
# Compile all modules
mvn compile

# Run all tests
mvn test

# Run a specific lab (example)
cd module1-foundations
mvn exec:java -Dexec.mainClass="com.concurrency.labs.lab08.CounterComparison"
```

## Learning Paths

### 🎓 Sequential Path (Recommended for Learning)

Go through modules in order - each builds on the previous:

| Step | Module | Labs | Key Concepts |
|------|--------|------|--------------|
| 1 | module1-foundations | 01-04, 22 | Race conditions, visibility, wait/notify, ThreadLocal |
| 2 | module2-locks-atomics | 05-10 | Locks, atomics, ConcurrentHashMap, BlockingQueue |
| 3 | module3-executors-async | 11-14, 21 | Executors, CompletableFuture, Fork/Join, Fan-Out/Fan-In |
| 4 | module4-liveness-production | 15-17 | Deadlock, graceful shutdown, debugging |
| 5 | module5-testing | 18-20 | Stress testing, race detection |
| 6 | classic-problems | Tier 1-4 | Interview classics (Queue, Cache, Pool, etc.) |
| 7 | projects | 1-3 | Capstone: Pipeline, Orchestrator, Scheduler |
| 8 | lld-concurrency | 3 problems | LLD + Concurrency: Booking, Parking, PubSub |

### 🎯 Interview-Priority Path (If Time is Short)

Focus on most commonly asked interview problems:

| Priority | Problem | Concepts Tested |
|----------|---------|-----------------|
| 1 | Bounded Blocking Queue | wait/notify, state machine |
| 2 | Token Bucket Rate Limiter | Time management, lazy-fill |
| 3 | Thread-Safe LRU Cache | Fine-grained locking, CHM |
| 4 | Custom Thread Pool | Worker pattern, BlockingQueue |
| 5 | Dining Philosophers | Deadlock prevention |

## In-Code Comment Guide

All skeleton files include pedagogical comments:

- `// TODO:` - What you need to implement
- `// 💡 THINK:` - Consider alternative approaches
- `// ⚠️ AVOID:` - Anti-patterns to understand and reject
- `// 🔑 HINT:` - Guidance for implementation
- `// 📝 NOTE:` - Important concepts to remember

## Project Structure

```
├── module1-foundations/       # Labs 01-04: Race conditions, visibility, wait/notify
├── module2-locks-atomics/     # Labs 05-10: Locks, atomics, CHM, queues
├── module3-executors-async/   # Labs 11-14: Executors, CompletableFuture, Fork/Join
├── module4-liveness-production/ # Labs 15-17: Deadlock, shutdown, debugging
├── module5-testing/           # Labs 18-20: Stress testing, race detection
├── classic-problems/          # 10 prioritized interview problems
├── projects/                  # 3 capstone projects
└── lld-concurrency/           # LLD + Concurrency interview problems
```

## Classic Problems by Tier

### 🔴 Tier 1: Core Locking (MUST MASTER)
- Bounded Blocking Queue
- Custom Reader-Writer Lock
- Dining Philosophers

### 🟠 Tier 2: Thread Coordination
- Print In Order
- Cyclic Barrier (MapReduce)

### 🟡 Tier 3: System Components (SDE2 Sweet Spot)
- Thread-Safe LRU Cache
- Token Bucket Rate Limiter
- Custom Thread Pool
- Delayed Task Scheduler

### 🟢 Tier 4: Java Specifics
- Double-Checked Locking Singleton

## LLD + Concurrency Problems

These combine OOP class design with thread-safety requirements - exactly what SDE2 interviews test:

| Problem | Concurrency Challenge | Similar To |
|---------|----------------------|------------|
| **Seat Booking System** | Prevent double-booking (CAS) | BookMyShow, movie tickets |
| **Parking Lot** | Multiple gates, atomic allocation | Classic LLD problem |
| **Pub-Sub System** | Concurrent publishers/subscribers | Mini-Kafka, event systems |
