# Alumni LinkedIn Profile Searcher

A Spring Boot REST API that searches for LinkedIn alumni profiles from a specific educational institution using the **PhantomBuster API**, and persists results to PostgreSQL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 / Java 21 |
| Persistence | Spring Data JPA + PostgreSQL |
| HTTP Client | Spring `RestTemplate` |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5 + Mockito + MockMvc |

---

## Project Structure

```
src/
├── main/java/com/alumni/search/
│   ├── AlumniSearchApplication.java      # Entry point
│   ├── client/
│   │   └── PhantomBusterClient.java      # PhantomBuster API integration
│   ├── config/
│   │   └── AppConfig.java                # RestTemplate bean
│   ├── controller/
│   │   └── AlumniController.java         # REST endpoints
│   ├── dto/
│   │   ├── AlumniProfileDto.java         # Response DTO
│   │   ├── AlumniSearchRequest.java      # Request DTO + validation
│   │   ├── ApiResponse.java              # Generic response wrapper
│   │   └── PhantomBusterResponse.java    # PhantomBuster response model
│   ├── exception/
│   │   ├── AlumniNotFoundException.java
│   │   ├── GlobalExceptionHandler.java   # Centralised error handling
│   │   └── PhantomBusterException.java
│   ├── model/
│   │   └── AlumniProfile.java            # JPA entity
│   ├── repository/
│   │   └── AlumniProfileRepository.java  # JPA repository
│   └── service/
│       ├── AlumniMapper.java             # Entity <-> DTO mapping
│       ├── AlumniService.java            # Service interface
│       └── AlumniServiceImpl.java        # Service implementation
└── test/java/com/alumni/search/
    ├── client/PhantomBusterClientTest.java
    ├── controller/AlumniControllerTest.java
    └── service/
        ├── AlumniMapperTest.java
        └── AlumniServiceImplTest.java
```

---

## Setup

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### 1. Create Database

```sql
CREATE DATABASE alumni_db;
```

### 2. Configure Environment Variables

```bash
export DB_URL=jdbc:postgresql://localhost:5432/alumni_db
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export PHANTOMBUSTER_API_KEY=your_phantombuster_api_key
export PHANTOMBUSTER_AGENT_ID=your_agent_id
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

---

## API Reference

### POST `/api/alumni/search`

Search alumni profiles via PhantomBuster. Results are persisted to the database.

**Request Body:**
```json
{
  "university": "University of XYZ",
  "designation": "Software Engineer",
  "passoutYear": 2020
}
```
> `passoutYear` is optional.

**Success Response (200):**
```json
{
  "status": "success",
  "data": [
    {
      "name": "John Doe",
      "currentRole": "Software Engineer",
      "university": "University of XYZ",
      "location": "New York, NY",
      "linkedinHeadline": "Software Engineer at XYZ Corp",
      "linkedinUrl": "https://linkedin.com/in/johndoe",
      "passoutYear": 2020
    }
  ]
}
```

**Error Responses:**

| HTTP Code | errorCode | Reason |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Missing/invalid request fields |
| 502 | `TIMEOUT` | PhantomBuster agent timed out |
| 502 | `UNAUTHORIZED` | Invalid PhantomBuster API key |
| 502 | `AGENT_ERROR` | PhantomBuster agent returned an error |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

---

### GET `/api/alumni/all`

Returns all alumni profiles stored in the database.

**Success Response (200):**
```json
{
  "status": "success",
  "data": [ ... ]
}
```

---

## Testing with Postman

1. Import the base URL: `http://localhost:8080`
2. **Search:** `POST /api/alumni/search` with JSON body
3. **List all:** `GET /api/alumni/all`

---

## Running Tests

```bash
mvn test
```

Tests include:
- **Unit tests** for `AlumniServiceImpl`, `AlumniMapper`, `PhantomBusterClient`
- **MockMvc integration tests** for `AlumniController` covering happy paths, validation errors, and upstream failures

---

## Design Decisions

- **Service interface + impl** — allows mocking in tests and swapping implementations
- **Upsert pattern** — prevents duplicate records; updates existing profiles matched by LinkedIn URL
- **Poll-based PhantomBuster integration** — mirrors PhantomBuster's async agent model with configurable timeout
- **Generic `ApiResponse<T>` wrapper** — consistent envelope for all endpoints
- **`GlobalExceptionHandler`** — centralised error handling, no try-catch in controllers
