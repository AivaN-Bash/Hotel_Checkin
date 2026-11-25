# Architecture

```mermaid
flowchart LR
  Guest[Guest on kiosk] --> Vercel[React + Vite static app on Vercel]
  Vercel -->|HTTPS rewrite /api/*| API[Spring Boot REST API on Koyeb]
  API -->|JDBC + Flyway| DB[(Supabase PostgreSQL)]
  API -->|/api/v1/status and /api/v1/health| Vercel
```

The browser owns the temporary kiosk flow and keeps no booking data in persistent browser storage. The API validates requests and returns DTOs. PostgreSQL is authoritative for booking and room state. A transaction locks the booking and selects a compatible room with `FOR UPDATE SKIP LOCKED`, so concurrent check-ins cannot assign the same room. An idempotency key makes retries return the original result.

The runtime maintenance switch lives in memory so it remains usable while PostgreSQL is unavailable. The status endpoint has a short database connection timeout. The frontend polls it, distinguishes a cold start from a sustained outage, and preserves the guest's current step for up to five minutes.

## Data model

```mermaid
erDiagram
  BOOKINGS ||--o| CHECKINS : receives
  ROOMS ||--o| CHECKINS : assigned_to
  BOOKINGS {
    uuid id PK
    string booking_code UK
    string guest_first_name
    string guest_last_name
    string phone_number
    string room_type
    date check_in_date
    date check_out_date
    int guest_count
    string status
  }
  ROOMS {
    uuid id PK
    string room_number UK
    int floor_number
    string room_type
    string status
  }
  CHECKINS {
    uuid id PK
    uuid booking_id FK
    uuid room_id FK
    string idempotency_key UK
    timestamp created_at
  }
```

Flyway owns schema creation and the fake demo seed. Hibernate validates the schema at startup and does not mutate it.
