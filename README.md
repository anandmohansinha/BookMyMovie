# BookMyMovie

Spring Boot backend for a movie ticket booking platform.

This codebase is now trimmed to keep only these implemented scenarios:

- Read scenario: browse theatres currently running a selected movie in a selected city on a selected date, including show timings
- Write scenario: book movie tickets by selecting a show and preferred seats for the day

The extra optional scenarios were removed from the active code/documentation:

- theatre-side update/delete show workflow for the day
- bulk booking and bulk cancellation
- manual theatre seat inventory allocation/update for a show
- payment workflow and customer booking-history APIs

## Project Structure

```text
src/main/java/com/example/movieticketbooking
|- config
|- controller
|- dto
|  |- browse
|  |- request
|  |- response
|  |- show
|- entity
|- enums
|- exception
|- mapper
|- repository
|- scheduler
|- service
|  |- impl
|- util
```

The project keeps the same functional behavior, but the structure is intentionally kept lean:

- one Spring Boot application class enables scheduling and configuration properties
- services contain the main business rules
- DTOs are used only for API contracts
- Redis, Kafka, Prometheus/tracing, and custom logging remain enabled

## Technology Stack

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA / Hibernate
- MySQL
- Redis for temporary lock cache
- Kafka for booking-created events
- Spring Boot Actuator / Micrometer
- Prometheus metrics
- Micrometer Tracing with Zipkin export
- Maven
- H2 for tests
- Swagger UI / OpenAPI

## Kept Functional Scope

### Read scenario

Browse theatres currently running the selected movie in the selected city on the selected date.

API:

```text
GET /api/browse/shows?movieId={movieId}&cityId={cityId}&showDate={yyyy-MM-dd}
```

Response includes:

- theatres in that city running the movie
- show timings
- screen name

### Write scenario

Book movie tickets by selecting preferred seats for a show.

Kept APIs:

```text
GET  /api/shows/{showId}/seats
POST /api/bookings/lock-seats
POST /api/bookings
GET  /api/bookings/{bookingId}
```

Flow:

1. Browse shows by city, movie, and date
2. Get seat layout for a selected show
3. Lock preferred seats temporarily
4. Create booking from the valid lock reference
5. Seats become `BOOKED`

## Booking Design

### Show-level inventory

Each show has independent seat inventory in `show_seat_inventory`.

Statuses:

- `AVAILABLE`
- `LOCKED`
- `BOOKED`
- `BLOCKED`

### Double-booking prevention

Seat locking is concurrency-safe.

- Requested seats are updated from `AVAILABLE` to `LOCKED` in one transaction
- The update only succeeds for seats still available at that exact moment
- If the updated row count is less than the requested seat count, the request fails
- This prevents two users from locking the same seat at the same time

### Lock expiry

Temporary locks are auto-released by the scheduler if the booking is not completed in time.

### Redis integration

Redis is now applied as a temporary lock cache with TTL.

- after a successful DB seat lock, the lock reference is cached in Redis
- seat-level Redis keys are created per show seat with the same expiry
- when a booking is created or a stale lock is released, those Redis keys are removed
- the relational DB remains the source of truth, so booking still stays correct even if Redis is unavailable

### Kafka integration

Kafka is now applied for asynchronous booking events.

- after a booking is created, a `booking-created` event is published
- the event contains booking, customer, show, theatre, and seat details
- this is designed for future notification, analytics, or downstream consumers
- Kafka publishing is best-effort and does not block the booking response

## Observability

### Structured logging

Structured key-value console logging is configured in [logback-spring.xml](/F:/2026_1/BookMyMovie/src/main/resources/logback-spring.xml).

Each log line includes:

- timestamp
- level
- application name
- correlation ID
- trace ID
- span ID
- thread
- logger
- message

### Correlation IDs with MDC

[CorrelationIdFilter.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/config/CorrelationIdFilter.java) reads `X-Correlation-Id` from the request or generates one if missing, stores it in MDC, and echoes it back in the response.

This makes request logs easier to trace across controllers and services.

### Metrics and Prometheus

[MetricsConfig.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/config/MetricsConfig.java) adds common Micrometer tags.

Booking flow metrics are recorded in [BookingServiceImpl.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/service/impl/BookingServiceImpl.java), including:

- seat lock attempts, successes, failures, and duration
- booking creation attempts, successes, failures, and duration

[PrometheusEndpointConfig.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/config/PrometheusEndpointConfig.java) registers the Prometheus scrape endpoint.

Prometheus endpoint:

- [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

### Distributed tracing

Micrometer tracing is enabled and exports spans to Zipkin when available.

Relevant properties in [application.yml](/F:/2026_1/BookMyMovie/src/main/resources/application.yml):

- `management.tracing.enabled`
- `management.tracing.sampling.probability`
- `management.zipkin.tracing.endpoint`

Trace and span IDs are automatically included in logs.

Configurable properties:

- `BOOKING_LOCK_DURATION_MINUTES`
- `BOOKING_CONVENIENCE_FEE_PER_SEAT`
- `BOOKING_LOCK_CLEANUP_CRON`

## Main Entities

- `City`
- `Theatre`
- `Screen`
- `Seat`
- `Movie`
- `Show`
- `Customer`
- `ShowSeatInventory`
- `Booking`
- `BookingSeat`

## Configuration

Default config is in [application.yml](/F:/2026_1/BookMyMovie/src/main/resources/application.yml).

Example PowerShell setup:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/book_my_movie?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
$env:BOOKING_LOCK_DURATION_MINUTES="5"
$env:BOOKING_CONVENIENCE_FEE_PER_SEAT="30"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:TRACING_ENABLED="true"
$env:TRACING_SAMPLING_PROBABILITY="1.0"
$env:ZIPKIN_ENDPOINT="http://localhost:9411/api/v2/spans"
```

## Run Instructions

1. Start MySQL.
2. Build:

```bash
mvn clean install
```

3. Run:

```bash
mvn spring-boot:run
```

Swagger:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)
- [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

## APIs and Curl Examples

### Create Show

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

When a show is created, seat inventory rows are automatically generated for that show.

### Browse Theatres and Show Timings

```bash
curl "http://localhost:8080/api/browse/shows?movieId=1&cityId=1&showDate=2026-04-26"
```

### View Seat Layout for a Show

```bash
curl http://localhost:8080/api/shows/1/seats
```

### Lock Seats

```bash
curl -X POST http://localhost:8080/api/bookings/lock-seats \
  -H "Content-Type: application/json" \
  -d '{
    "showId": 1,
    "seatNumbers": ["A1", "A2"],
    "customer": {
      "name": "Anand",
      "email": "anand@example.com",
      "phone": "9999999999"
    }
  }'
```

### Create Booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "lockReference": "LCK-20260424120000-ABCD1234",
    "customer": {
      "name": "Anand",
      "email": "anand@example.com",
      "phone": "9999999999"
    }
  }'
```

### Get Booking Details

```bash
curl http://localhost:8080/api/bookings/1
```

## Tests

Run:

```bash
mvn test
```

Current automated coverage includes:

- browse scenario
- show seat layout
- successful seat lock
- failed seat lock when seat is already locked
- booking creation from lock reference
- expired lock rejection
- lock cleanup job
- concurrent same-seat lock conflict where only one request succeeds
- Prometheus scrape endpoint exposure
- correlation ID echo handling

## Key Files

- [BrowseController.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/controller/BrowseController.java)
- [ShowController.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/controller/ShowController.java)
- [BookingController.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/controller/BookingController.java)
- [BookingServiceImpl.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/service/impl/BookingServiceImpl.java)
- [BrowseServiceImpl.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/service/impl/BrowseServiceImpl.java)
- [RedisSeatLockCacheService.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/service/impl/RedisSeatLockCacheService.java)
- [KafkaBookingEventPublisher.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/service/impl/KafkaBookingEventPublisher.java)
- [CorrelationIdFilter.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/config/CorrelationIdFilter.java)
- [MetricsConfig.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/config/MetricsConfig.java)
- [PrometheusEndpointConfig.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/config/PrometheusEndpointConfig.java)
- [LockExpiryScheduler.java](/F:/2026_1/BookMyMovie/src/main/java/com/example/movieticketbooking/scheduler/LockExpiryScheduler.java)
- [MovieTicketBookingIntegrationTest.java](/F:/2026_1/BookMyMovie/src/test/java/com/example/movieticketbooking/controller/MovieTicketBookingIntegrationTest.java)
