# BookMyMovie - Phase 1

Spring Boot 3 / Java 17 backend for a movie ticket booking platform focused on master data and browse flow only.

## Features

- Create and list cities
- Create and list theatres with nested screens and seats
- Create and list movies
- Create shows
- Browse theatres and show timings by movie, city, and date
- Validation, layered architecture, centralized exception handling, logging, and sample seed data
- Business rules:
  - only active movies can be scheduled
  - no overlapping shows on the same screen for the same date

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- H2 for tests

## Project Structure

```text
src/main/java/com/example/movieticketbooking
|- config
|- controller
|- dto
|- entity
|- enums
|- exception
|- repository
|- service
```

## Database Setup

1. Create a PostgreSQL database:

```sql
CREATE DATABASE movie_booking;
```

2. Configure environment variables if you do not want the defaults:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/movie_booking"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
```

Default values are already present in `src/main/resources/application.yml`.

## Run Instructions

1. Build the project:

```bash
mvn clean install
```

2. Run the application:

```bash
mvn spring-boot:run
```

3. The API will be available at:

```text
http://localhost:8080
```

Sample seed data is loaded automatically when the database is empty. Disable it with:

```powershell
$env:APP_SEED_ENABLED="false"
```

## Test Instructions

Run all tests:

```bash
mvn test
```

Tests use H2 in-memory database with the `test` profile from `src/test/resources/application-test.yml`.

## REST APIs

### 1. Create City

```bash
curl -X POST http://localhost:8080/api/cities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pune",
    "state": "Maharashtra",
    "country": "India"
  }'
```

### 2. List Cities

```bash
curl http://localhost:8080/api/cities
```

### 3. Create Theatre

```bash
curl -X POST http://localhost:8080/api/theatres \
  -H "Content-Type: application/json" \
  -d '{
    "cityId": 1,
    "name": "City Center Cinemas",
    "address": "MG Road, Pune",
    "screens": [
      {
        "name": "Screen 1",
        "seats": [
          { "rowLabel": "A", "seatNumber": 1, "seatType": "REGULAR" },
          { "rowLabel": "A", "seatNumber": 2, "seatType": "REGULAR" },
          { "rowLabel": "B", "seatNumber": 1, "seatType": "PREMIUM" }
        ]
      },
      {
        "name": "Screen 2",
        "seats": [
          { "rowLabel": "A", "seatNumber": 1, "seatType": "REGULAR" },
          { "rowLabel": "A", "seatNumber": 2, "seatType": "RECLINER" }
        ]
      }
    ]
  }'
```

### 4. List Theatres

```bash
curl http://localhost:8080/api/theatres
```

Filter by city:

```bash
curl "http://localhost:8080/api/theatres?cityId=1"
```

### 5. Create Movie

```bash
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Neon Run",
    "language": "English",
    "genre": "Action",
    "durationMinutes": 135,
    "certification": "UA",
    "description": "A courier gets pulled into a city-wide conspiracy overnight.",
    "releaseDate": "2026-04-20",
    "status": "ACTIVE"
  }'
```

### 6. List Movies

```bash
curl http://localhost:8080/api/movies
```

### 7. Create Show

```bash
curl -X POST http://localhost:8080/api/shows \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "screenId": 1,
    "showDate": "2026-04-26",
    "startTime": "10:00:00",
    "endTime": "12:20:00",
    "ticketPrice": 250.00
  }'
```

### 8. Browse Theatres and Show Timings

```bash
curl "http://localhost:8080/api/browse/shows?movieId=1&cityId=1&showDate=2026-04-26"
```

## Notes

- This phase does not include booking, payment, seat locking, coupons, or offers.
- `POST /api/theatres` is designed to create master data for theatre, screens, and seats in one request.
- Show overlap is blocked when a new show starts before an existing show ends and ends after an existing show starts.
