# Java Concurrency Labs - SDE2 Interview Prep

> **AI-Generated Repository**: This project was created with AI assistance. Review all code before production use.

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

# Run tests for a specific module
cd module1-foundations
mvn test
```

## Learning Path

| Week | Focus | Modules |
|------|-------|---------|
| 1 | Core Signaling | Module 1 + Classic Problem #1 |
| 2 | System Components | Classic Problems #7, #8 |
| 3 | Fine-grained Locking | Module 2 + Classic Problem #6 |
| 4 | Deadlock Prevention | Module 4 + Classic Problem #3 |
| 5 | Coordination | Classic Problems #4, #5 + Module 3 |
| 6 | Testing & Projects | Module 5 + Projects |

## In-Code Comment Guide

All skeleton files include pedagogical comments:

- `// TODO:` - What you need to implement
- `// 💡 THINK:` - Consider alternative approaches
- `// ⚠️ AVOID:` - Anti-patterns to understand and reject
- `// 🔑 HINT:` - Guidance for implementation
- `// 📝 NOTE:` - Important concepts to remember

## Project Structure

```
├── module1-foundations/       # Race conditions, visibility, wait/notify
├── module2-locks-atomics/     # Locks, atomics, CHM, queues
├── module3-executors-async/   # Executors, CompletableFuture, Fork/Join
├── module4-liveness-production/ # Deadlock, shutdown, debugging
├── module5-testing/           # Stress testing, race detection
└── classic-problems/          # Prioritized interview problems
```

## Priority Problems (Start Here!)

1. **Bounded Blocking Queue** - Core signaling
2. **Token Bucket Rate Limiter** - Time management
3. **Thread-Safe LRU Cache** - Fine-grained locking
