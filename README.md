# Student Planner API

The backend service for **Student Planner** — an application that helps students organize their academic work through tasks with deadlines, progress tracking, and drag-and-drop ordering.

This repository contains the **backend only**, built with Spring Boot. The desktop client lives in a separate repo: [TaskPlannerFrontEnd](https://github.com/khuslee0409/TaskPlannerFrontEnd).

---

## Features

- **User authentication** — registration, login, and JWT-based session handling
- **Email verification** — 6-digit codes sent on signup (via [Resend](https://resend.com)), required before login
- **Password reset flow** — request code → verify code → get a short-lived reset token → set new password
- **Task management**
  - Create tasks with a title and deadline
  - List active (incomplete) tasks, ordered by position
  - Rename a task
  - Update progress (0–100%)
  - Mark a task complete (soft delete — completed tasks are excluded from the active list, not removed)
  - Reorder tasks (drag-and-drop support)
- **Per-user data isolation** — every task is scoped to the authenticated user via the JWT

---

## Tech Stack

| Layer            | Technology                          |
|-------------------|--------------------------------------|
| Language          | Java 21                              |
| Framework         | Spring Boot 4.0.2                    |
| Build Tool        | Maven (via Maven Wrapper `mvnw`)     |
| Data Access       | Spring Data JPA (Hibernate)          |
| Database          | MySQL                                |
| Auth              | Spring Security + JWT (`jjwt` 0.12.5)|
| Password Hashing  | BCrypt                               |
| Validation        | Jakarta Bean Validation              |
| Transactional Email | Resend API                         |
| Deployment        | Railway (reads config from env vars) |
| Testing           | Spring Boot Test, Spring Security Test |

---

## Prerequisites

- **Java 21** (JDK)
- **MySQL** (running locally or accessible remotely)
- A [Resend](https://resend.com) account + API key (for sending verification/reset emails)
- No need to install Maven separately — this project uses the Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/khuslee0409/TaskPlanner.git
cd TaskPlanner/student-planner-api
```

### 2. Configure environment variables

This app reads its configuration entirely from environment variables (`src/main/resources/application.properties` references them, it doesn't hardcode values). Set the following before running:

| Variable | Description | Example |
|---|---|---|
| `PORT` | Server port (defaults to `8080` if unset) | `8080` |
| `DB_URL` | JDBC URL for your MySQL database | `jdbc:mysql://localhost:3306/student_planner_db` |
| `DB_USER` | MySQL username | `root` |
| `DB_PASS` | MySQL password | `yourpassword` |
| `APP_JWT_SECRET` | Secret key used to sign JWTs (use a long, random string) | `a-very-long-random-secret-string` |
| `APP_JWT_EXP_MINUTES` | JWT expiration time in minutes (defaults to `120`) | `120` |
| `RESEND_API_KEY` | API key from your Resend account | `re_xxxxxxxx` |
| `MAIL_FROM` | Verified sender address for Resend | `Student Planner <noreply@yourdomain.xyz>` |

Create the database first:

```sql
CREATE DATABASE student_planner_db;
```

You can export these directly in your shell, use a `.env` loader, or configure them in your IDE's run configuration — whichever fits your workflow. `spring.jpa.hibernate.ddl-auto=update` is set, so tables are created/updated automatically from the JPA entities on startup.

### 3. Run the application

Using the Maven Wrapper:

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The API will start on:

```
http://localhost:8080
```

### 4. Run tests

```bash
./mvnw test
```

---

## Project Structure

```
student-planner-api/
└── src/main/java/com/khuslee/student_planner_api/
    ├── StudentPlannerApiApplication.java   # Entry point
    ├── controller/
    │   └── AuthController.java             # /api/auth/** endpoints
    ├── task/
    │   ├── TaskController.java             # /api/tasks/** endpoints
    │   ├── TaskService.java                # Task business logic
    │   ├── TaskEntity.java                 # JPA entity (tasks table)
    │   ├── TaskRepository.java             # Spring Data JPA repository
    │   ├── CreateTaskRequest.java
    │   ├── RenameTaskRequest.java
    │   └── UpdateProgressRequest.java
    ├── user/
    │   ├── UserEntity.java                 # JPA entity (users table)
    │   └── UserRepository.java
    ├── auth/                               # Auth request/response DTOs
    │   ├── RegisterRequest.java / LoginRequest.java / AuthResponse.java
    │   ├── ForgotPasswordRequest.java / ResetPasswordRequest.java
    │   └── VerifyCodeRequest.java / VerifyResetCodeRequest.java / VerifyResetCodeResponse.java
    ├── UserService/
    │   ├── UserService.java                # Registration, login, verification, password reset logic
    │   └── EmailService.java               # Sends transactional email via Resend
    ├── security/
    │   ├── SecurityConfig.java             # Spring Security filter chain (stateless, JWT-based)
    │   ├── SecurityBeans.java              # PasswordEncoder (BCrypt) bean
    │   ├── JwtService.java                 # Token generation/validation
    │   ├── JwtAuthFilter.java              # Per-request JWT auth filter
    │   └── DbUserDetailsService.java       # Loads users for Spring Security
    └── config/
        └── Async.java                      # Enables @Async (used for non-blocking email sending)
```

---

## API Endpoints

Base URL: `http://localhost:8080`

### Auth — `/api/auth` (public, no token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user. Sends an email verification code. |
| POST | `/api/auth/verify-code` | Verify email using the 6-digit code. Required before first login. |
| POST | `/api/auth/login` | Log in with username + password. Returns a JWT. |
| POST | `/api/auth/forgot-password` | Request a password reset code by email. |
| POST | `/api/auth/verify-reset-code` | Verify the reset code; returns a short-lived reset token. |
| POST | `/api/auth/reset-password` | Set a new password using the reset token. |

### Tasks — `/api/tasks` (requires `Authorization: Bearer <token>`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all active (incomplete) tasks for the logged-in user, ordered by position. |
| POST | `/api/tasks` | Create a new task. Body: `{ "title": "...", "deadline": "dd-MM-yyyy hh:mm a" }` |
| PUT | `/api/tasks/{id}/rename` | Rename a task. Body: `{ "title": "..." }` |
| PUT | `/api/tasks/{id}/progress` | Update task progress. Body: `{ "progress": 0-100 }` |
| PUT | `/api/tasks/{id}/complete` | Mark a task as completed. |
| PUT | `/api/tasks/reorder` | Reorder tasks. Body: array of task IDs in the new order, e.g. `[3, 1, 2]` |

All task endpoints identify the user from the JWT (via `Principal`) — there's no need to pass a user ID in the request body or URL.

**Auth header example:**
```
Authorization: Bearer <jwt-token-from-login>
```

---

## Authentication Flow

1. **Register** → `POST /api/auth/register` → verification code emailed to the user
2. **Verify email** → `POST /api/auth/verify-code` with the code
3. **Login** → `POST /api/auth/login` → returns `{ "token": "<jwt>" }`
4. Include the token as `Authorization: Bearer <jwt>` on all `/api/tasks/**` requests

**Forgot password:**
1. `POST /api/auth/forgot-password` with the email → reset code sent
2. `POST /api/auth/verify-reset-code` with the code → returns a one-time reset token
3. `POST /api/auth/reset-password` with the token + new password

---

## Related Repositories

| Repo | Description |
|------|-------------|
| [TaskPlannerFrontEnd](https://github.com/khuslee0409/TaskPlannerFrontEnd) | The desktop client for Student Planner, built with **JavaFX** (FXML views + Java controllers). Consumes this API over HTTP. |

To run the full application, start this backend first (so the database and API are up), then launch the JavaFX frontend so it can connect to `http://localhost:8080`.

---

## Deployment

The app is set up to run on **Railway** (or any host that provides environment variables and a MySQL instance) — `application.properties` reads `PORT`, `DB_URL`, `DB_USER`, `DB_PASS`, `APP_JWT_SECRET`, `APP_JWT_EXP_MINUTES`, `RESEND_API_KEY`, and `MAIL_FROM` directly from the environment, so no code changes are needed between local and production as long as those variables are set.

---

## License

No license specified yet.
