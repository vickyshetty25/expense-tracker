# Expense Tracker API

A REST API for tracking personal expenses, setting monthly budgets, and generating spending reports by category.

Built as a backend engineering project to understand the full stack of a real expense management system.

---

## Features

- JWT authentication with BCrypt password encoding
- Expense CRUD with category and date filters + pagination
- Monthly budget setting with overspend alerts
- Redis-cached monthly spending report (per user/month/year)
- Ownership check — users can only access their own data
- Global exception handling with meaningful error responses
- Dockerised stack — one command to run everything

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.4 |
| Spring Security + JWT | jjwt 0.11.5 |
| MySQL | 9.6 |
| Redis | 7 |
| Docker + docker-compose | latest |
| JUnit 5 + Mockito | - |
| Maven | - |

---

## How to Run
```bash
git clone https://github.com/vickyshetty25/expense-tracker.git
cd expense-tracker
docker-compose up --build
```

API available at: `http://localhost:8080`

Import `postman/expense-tracker-api.json` to test all endpoints.

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/auth/register | No | Register new user |
| POST | /api/auth/login | No | Login and get JWT token |
| GET | /api/categories | Yes | Get all categories |
| POST | /api/categories | Yes | Create a category |
| POST | /api/expenses | Yes | Create an expense |
| GET | /api/expenses | Yes | Get expenses (filter by category, date, page) |
| GET | /api/expenses/{id} | Yes | Get expense by ID |
| PUT | /api/expenses/{id} | Yes | Update an expense |
| DELETE | /api/expenses/{id} | Yes | Delete an expense |
| POST | /api/budgets | Yes | Set monthly budget for a category |
| GET | /api/budgets/alert | Yes | Get overspend alerts for a month |
| GET | /api/reports/monthly | Yes | Get monthly spending report (Redis cached) |

---

## Sample Response — Monthly Report
```json
{
    "month": 3,
    "year": 2026,
    "totalSpent": 1250.00,
    "spentByCategory": {
        "Food": 450.00,
        "Transport": 300.00,
        "Shopping": 500.00
    }
}
```

---

## Why Redis Caching?

The monthly report aggregates all expenses for a month — it does a GROUP BY with SUM across potentially hundreds of records.

- First call: hits MySQL, result cached in Redis with 1-hour TTL
- Second call: returns in under 5ms from Redis vs ~80ms from MySQL

Cache is evicted automatically when an expense is created, updated, or deleted.

---

## Security

Every endpoint that modifies data checks ownership:
```java
if (!expense.getUser().getId().equals(loggedInUserId))
    throw new ForbiddenException("You do not own this resource");
```

This prevents users from accessing or modifying other users' data.