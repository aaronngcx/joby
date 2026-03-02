# Joby - Job Queue System

A mini Sidekiq-like job queue system built with Spring Boot 3 and Java 21.

## Features
- Enqueue jobs with type, payload and priority
- Concurrent job processing with thread pool
- Automatic retry with configurable max attempts
- Priority-based job scheduling
- Job status tracking (PENDING, RUNNING, DONE, FAILED, DEAD)
- REST API with Swagger UI documentation

## Tech Stack
- Java 21
- Spring Boot 3
- PostgreSQL
- Flyway (database migrations)
- Docker + Docker Compose
- Lombok

## Running Locally

### Prerequisites
- Docker Desktop
- Java 21

### Start the app
```bash