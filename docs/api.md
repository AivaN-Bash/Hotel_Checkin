# API reference

Base path: `/api/v1`. Requests and responses use JSON. Springdoc serves the OpenAPI UI at `/swagger-ui` and its document at `/v3/api-docs`.

| Method | Path | Purpose | Access |
| --- | --- | --- | --- |
| `POST` | `/bookings/lookup` | Find a confirmed booking by code or last name plus phone | Public kiosk |
| `POST` | `/bookings/checkout-lookup` | Find an active checked-in stay for checkout | Public kiosk |
| `POST` | `/checkins` | Check in and assign an available matching room | Public kiosk; requires `Idempotency-Key` |
| `POST` | `/checkouts` | Check out a stay, record its checkout time, and release the room | Public kiosk |
| `GET` | `/checkins/{id}` | Read a completed check-in | Public kiosk |
| `GET` | `/rooms` | List rooms and availability | `X-Admin-Key` |
| `GET` | `/status` | API, database, and maintenance state | Public, remains available in maintenance |
| `GET` | `/health` | Liveness response | Public, remains available in maintenance |
| `POST` | `/admin/maintenance` | Change in-memory maintenance state | `X-Admin-Key` |

## Look up by booking code

```http
POST /api/v1/bookings/lookup
Content-Type: application/json

{"bookingCode":"DEMO2026"}
```

```json
{
  "id": "10000000-0000-0000-0000-000000000001",
  "guestName": "Alex Rivera",
  "roomType": "Deluxe King",
  "checkInDate": "<today>",
  "checkOutDate": "<today plus 2 days>",
  "guestCount": 2
}
```

Name lookup accepts `{"lastName":"Rivera","phoneNumber":"+66000000001"}`. The phone is used only as a booking lookup value; the demo does not collect identity documents or payment details.

## Check in

```http
POST /api/v1/checkins
Content-Type: application/json
Idempotency-Key: kiosk-session-unique-key

{"bookingId":"10000000-0000-0000-0000-000000000001"}
```

The same key and request return the original check-in on retry. A key reused for another booking returns a conflict.

## Check out

The kiosk finds active stays through `POST /api/v1/bookings/checkout-lookup` using the same booking-code or guest-details payload as regular lookup. To finish checkout, it posts `{"bookingId":"<booking UUID>"}` to `POST /api/v1/checkouts`. The operation is transactional: it marks the booking `CHECKED_OUT`, records `checked_out_at` on its check-in row, and returns the assigned room to `AVAILABLE`.

```json
{
  "id": "30000000-0000-0000-0000-000000000001",
  "guestName": "Alex Rivera",
  "roomType": "Deluxe King",
  "roomNumber": "201",
  "floor": 2,
  "digitalKey": "DEMO-KEY-30000000-0000-0000-0000-000000000001",
  "createdAt": "<ISO-8601 timestamp>"
}
```

## Status and maintenance

```json
{
  "status": "DEGRADED",
  "components": {"api": "UP", "database": "DOWN"},
  "message": "The booking system is temporarily unavailable. Please visit the front desk for assistance.",
  "estimatedBackAt": null,
  "checkedAt": "<ISO-8601 timestamp>"
}
```

`/status` uses HTTP 200 when the database is down so the kiosk can render the component state. Operational API/database errors elsewhere use HTTP 503 and the common error body: `{"timestamp":"<ISO-8601>","status":503,"code":"SERVICE_UNAVAILABLE","message":"The booking system is temporarily unavailable. Please try again shortly."}`. Validation failures use 400; a wrong admin key uses 401.

To enable maintenance, use a key stored in the server's `ADMIN_API_KEY` environment variable (at least 32 characters):

```sh
curl -X POST "$API_URL/api/v1/admin/maintenance" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: $ADMIN_API_KEY" \
  -d '{"enabled":true,"message":"We are preparing your room.","estimatedBackAt":"2030-01-01T12:00:00Z"}'
```

Set `enabled` to `false` to restore kiosk requests. The runtime setting resets on a backend restart; use `MAINTENANCE_MODE=true` when maintenance should start enabled after restart.
