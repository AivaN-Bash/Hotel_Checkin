-- Restore only seeded demo bookings and their stay records.
UPDATE rooms r
SET status = 'AVAILABLE'
WHERE r.status = 'OCCUPIED'
  AND EXISTS (
    SELECT 1
    FROM checkins c
    JOIN bookings b ON b.id = c.booking_id
    WHERE c.room_id = r.id
      AND b.booking_code IN ('DEMO2026', 'DEMO2027', 'DEMO2028')
  )
  AND NOT EXISTS (
    SELECT 1
    FROM checkins c
    JOIN bookings b ON b.id = c.booking_id
    WHERE c.room_id = r.id
      AND c.checked_out_at IS NULL
      AND b.booking_code NOT IN ('DEMO2026', 'DEMO2027', 'DEMO2028')
  );

DELETE FROM checkins
WHERE booking_id IN (
  SELECT id FROM bookings WHERE booking_code IN ('DEMO2026', 'DEMO2027', 'DEMO2028')
);

UPDATE bookings
SET status = CASE booking_code
  WHEN 'DEMO2026' THEN 'CONFIRMED'
  WHEN 'DEMO2027' THEN 'CONFIRMED'
  WHEN 'DEMO2028' THEN 'CHECKED_IN'
END
WHERE booking_code IN ('DEMO2026', 'DEMO2027', 'DEMO2028');

INSERT INTO checkins (id, booking_id, room_id, idempotency_key, created_at)
VALUES (
  '30000000-0000-0000-0000-000000000028',
  (SELECT id FROM bookings WHERE booking_code = 'DEMO2028'),
  (
    SELECT id
    FROM rooms r
    WHERE r.room_type = 'Deluxe King'
      AND r.status = 'AVAILABLE'
      AND NOT EXISTS (
        SELECT 1 FROM checkins c
        WHERE c.room_id = r.id AND c.checked_out_at IS NULL
      )
    ORDER BY CASE WHEN r.room_number = '301' THEN 0 ELSE 1 END,
      r.floor_number,
      r.room_number
    LIMIT 1
  ),
  'seed-demo-checkin-DEMO2028',
  CURRENT_TIMESTAMP
);

UPDATE rooms
SET status = 'OCCUPIED'
WHERE id = (
  SELECT room_id
  FROM checkins
  WHERE booking_id = (SELECT id FROM bookings WHERE booking_code = 'DEMO2028')
);
