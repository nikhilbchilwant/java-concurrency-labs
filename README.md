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

Go through labs in order - each builds on the previous:

| Module | Labs | Key Concepts |
|--------|------|--------------|
| Module 1 | Labs 01-04 | Race conditions, visibility, wait/notify |
| Module 2 | Labs 05-10 | Locks, atomics, ConcurrentHashMap, BlockingQueue |
| Module 3 | Labs 11-14 | Executors, CompletableFuture, Fork/Join |
| Module 4 | Labs 15-17 | Deadlock, graceful shutdown, debugging |
| Module 5 | Labs 18-20 | Testing concurrent code |
| Projects | 1-3 | Integration projects |

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
└── projects/                  # 3 capstone projects
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
