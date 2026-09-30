# Library Management System (RESTful API)

## Overview

The **Library Management System** is a robust, production-ready backend RESTful API built with **Spring Boot**, **Spring Security**, and **MySQL**. It provides secure identity management, token-based authentication (JWT), role-based access control (RBAC), and full lifecycle management for books and borrowing records. 

Designed following standard layered enterprise architecture (Controller-Service-Repository), the system ensures strict transactional integrity, input validation, defensive error handling, and authorization checks.

---

## Features

- **User Registration & Authentication**:
  - Secure member registration with input validation.
  - BCrypt password hashing before database persistence.
  - Rejection of duplicate usernames and email addresses.
  - Stateless JWT issuance upon verified login credentials.
- **Account Status Enforcement**:
  - Enforcement of active/disabled status (`enabled = false`) during login and on every request through the JWT filter.
- **Role-Based Authorization**:
  - Fine-grained permission boundaries across `ADMIN`, `LIBRARIAN`, and `MEMBER` roles.
- **Book Catalog Management**:
  - Adding, updating, retrieving, and deleting books.
  - Real-time book availability and stock tracking.
  - Copy validation (ensuring available copies never exceed total copies).
- **Borrow & Return Workflows**:
  - Transactional book borrowing with automatic copy decrement and due-date scheduling (14 days).
  - Availability validation (prevents borrowing when available copies are 0).
  - Secure return workflow enforcing **borrower ownership** (members can only return their own borrowed books).
  - Automatic inventory restoration upon return.
  - Member borrow history tracking and system-wide audit records.
- **Centralized Exception Handling**:
  - `@RestControllerAdvice` delivering consistent JSON error responses with standard HTTP status codes (`400`, `401`, `403`, `404`, `409`, `500`).
- **Bean Validation**:
  - Jakarta Validation constraints (`@NotBlank`, `@Size`, `@Email`, `@Min`, `@NotNull`) on incoming payloads.
- **Automated Testing Suite**:
  - JUnit 5 and Mockito unit and controller slice tests verifying business rules, security boundaries, and validation.

---

## Tech Stack

- **Java**: 17 / 21+ (tested on Java 23)
- **Framework**: Spring Boot (Spring MVC, Spring Data JPA, Spring Security)
- **Security & Authentication**: Spring Security, JJWT (io.jsonwebtoken 0.12.6), BCrypt
- **Database & ORM**: MySQL, Hibernate 7, HikariCP (with H2 for self-contained test execution)
- **Validation**: Jakarta Bean Validation (Hibernate Validator)
- **Build Tool**: Apache Maven (via Maven Wrapper `mvnw`)

---

## Architecture

The application adheres to clean layered architecture:

```
[ Client / Postman / Frontend ]
               |
        HTTP Requests (with/without Bearer JWT)
               v
       [ SecurityFilterChain ] <---> [ JwtFilter ]
               v
     [ Controller Layer ] (REST API Endpoints, @Valid, DTOs)
               v
       [ Service Layer ] (Business Logic, Validation, @Transactional)
               v
      [ Repository Layer ] (Spring Data JPA Interfaces)
               v
      [ Database Layer ] (MySQL / H2 In-Memory)
```

### Authentication & Security Flow

1. **Registration**: Client sends `POST /user/register`. Input is validated, uniqueness verified, password hashed via BCrypt, and user persisted with `ROLE_MEMBER`.
2. **Login**: Client sends `POST /user/login`. Credentials and account active status (`enabled == true`) are checked. On success, a signed HMAC-SHA256 JWT is returned.
3. **Protected Requests**: Client passes `Authorization: Bearer <token>` header. `JwtFilter` verifies signature, expiration, and ensures user exists and remains enabled, then populates the `SecurityContextHolder`.

---

## Roles & Permissions

| Endpoint / Operation | Allowed Roles | Description |
| :--- | :--- | :--- |
| `POST /user/register` | Public | Register as a new Member |
| `POST /user/login` | Public | Authenticate and obtain JWT |
| `GET /book/all` | `ADMIN`, `LIBRARIAN`, `MEMBER` | View all cataloged books |
| `GET /book/available` | `ADMIN`, `LIBRARIAN`, `MEMBER` | View books with available copies |
| `GET /book/{id}` | `ADMIN`, `LIBRARIAN`, `MEMBER` | View specific book details |
| `POST /book/add` | `ADMIN`, `LIBRARIAN` | Add a new book to catalog |
| `PUT /book/update/{id}` | `ADMIN`, `LIBRARIAN` | Update book information and copies |
| `DELETE /book/delete/{id}` | `ADMIN` | Remove a book from catalog |
| `POST /borrow/{bookId}` | `MEMBER` | Borrow a book |
| `PUT /borrow/return/{borrowId}`| `MEMBER`, `LIBRARIAN`, `ADMIN` | Return book (Member restricted to own record) |
| `GET /borrow/my` | `MEMBER` | View logged-in member's borrow history |
| `GET /borrow/all` | `ADMIN`, `LIBRARIAN` | View all borrow records across library |
| `POST /admin/librarian` | `ADMIN` | Register a new Librarian staff member |
| `GET /admin/users` | `ADMIN` | View list of all system users |
| `GET /admin/members` | `ADMIN` | View list of all registered members |
| `GET /admin/librarians` | `ADMIN` | View list of all librarians |
| `GET /admin/user/{id}` | `ADMIN` | View specific user profile |
| `DELETE /admin/user/{id}` | `ADMIN` | Delete user record |
| `PUT /admin/disable/{id}` | `ADMIN` | Disable user account |
| `PUT /admin/enable/{id}` | `ADMIN` | Re-enable user account |

---

## API Overview

### User & Authentication (`/user`)
- `POST /user/register` — Register a member account
- `POST /user/login` — Login with username and password

### Books (`/book`)
- `GET /book/all` — Retrieve all books
- `GET /book/available` — Retrieve available books (copies > 0)
- `GET /book/{id}` — Retrieve book by ID
- `POST /book/add` — Add a new book
- `PUT /book/update/{id}` — Update book details
- `DELETE /book/delete/{id}` — Delete book by ID

### Borrowing (`/borrow`)
- `POST /borrow/{bookId}` — Borrow an available book
- `PUT /borrow/return/{borrowId}` — Return a borrowed book
- `GET /borrow/my` — Get current member's borrowing history
- `GET /borrow/all` — Get all library borrowing records

### Administration (`/admin`)
- `POST /admin/librarian` — Create a librarian user
- `GET /admin/users` — List all users
- `GET /admin/members` — List all members
- `GET /admin/librarians` — List all librarians
- `GET /admin/user/{id}` — Fetch user by ID
- `DELETE /admin/user/{id}` — Delete user by ID
- `PUT /admin/disable/{id}` — Disable user account
- `PUT /admin/enable/{id}` — Enable user account

---

## Setup Instructions

### Prerequisites
- **Java Development Kit (JDK)**: 17 or higher
- **MySQL Server**: 8.0+
- **Git** & **Maven** (or use included `mvnw` wrapper)

### 1. Database Setup
Log into your MySQL instance and create the database:

```sql
CREATE DATABASE library_db;
```

### 2. Configure Environment Variables
You can configure database credentials, JWT secret, and the seed admin account via environment variables or by editing `src/main/resources/application.properties`:

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true` |
| `DB_USERNAME` | MySQL user | `root` |
| `DB_PASSWORD` | MySQL password | *(empty)* |
| `JWT_SECRET` | HMAC-SHA256 Signing key | Secure default key (change in production) |
| `ADMIN_USERNAME` | Seed Admin Username | `admin` |
| `ADMIN_PASSWORD` | Seed Admin Password | `Admin@123` |
| `ADMIN_EMAIL` | Seed Admin Email | `admin@library.com` |

*(Note: On initial startup, if the database does not already contain an admin user, `AdminInitializer` creates a default admin account using the configured credentials above).*

### 3. Build the Project and Run Tests

Using the Maven wrapper:

```bash
# On Linux/macOS
./mvnw clean test

# On Windows
mvnw.cmd clean test
```

### 4. Run the Application

```bash
# On Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

The application starts on port `8081` (default): `http://localhost:8081`.

---

## Example API Flow

### 1. Register a Member
**Endpoint**: `POST http://localhost:8081/user/register`  
**Headers**: `Content-Type: application/json`  
**Body**:
```json
{
  "name": "Jane Doe",
  "username": "janedoe",
  "email": "jane@example.com",
  "password": "Password@123",
  "phone": "9876543210",
  "address": "123 Elm Street"
}
```
**Response** (`201 Created`):
```json
{
  "id": 2,
  "name": "Jane Doe",
  "username": "janedoe",
  "email": "jane@example.com",
  "role": "MEMBER",
  "enabled": true
}
```

### 2. Login
**Endpoint**: `POST http://localhost:8081/user/login`  
**Headers**: `Content-Type: application/json`  
**Body**:
```json
{
  "username": "janedoe",
  "password": "Password@123"
}
```
**Response** (`200 OK`):
```json
{
  "id": 2,
  "name": "Jane Doe",
  "username": "janedoe",
  "email": "jane@example.com",
  "role": "MEMBER",
  "enabled": true,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Access Protected Endpoint (e.g., View Available Books)
**Endpoint**: `GET http://localhost:8081/book/available`  
**Headers**:
- `Authorization`: `Bearer eyJhbGciOiJIUzI1NiJ9...`
- `Content-Type`: `application/json`

**Response** (`200 OK`):
```json
[
  {
    "book_id": 1,
    "title": "Clean Architecture",
    "author": "Robert C. Martin",
    "isbn": "978-0134494166",
    "category": "Software Engineering",
    "totalCopies": 5,
    "availableCopies": 4
  }
]
```

### 4. Borrow a Book
**Endpoint**: `POST http://localhost:8081/borrow/1`  
**Headers**: `Authorization: Bearer <TOKEN>`  
**Response** (`200 OK`):
```json
{
  "message": "Book borrowed successfully"
}
```

### 5. Return a Borrowed Book
**Endpoint**: `PUT http://localhost:8081/borrow/return/1`  
**Headers**: `Authorization: Bearer <TOKEN>`  
**Response** (`200 OK`):
```json
{
  "message": "Book returned successfully"
}
```
*(If a member attempts to return another user's borrow ID, the API rejects the request with `403 Forbidden: Unauthorized: You can only return books borrowed by your own account`).*
