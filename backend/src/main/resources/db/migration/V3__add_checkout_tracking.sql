ALTER TABLE checkins ADD COLUMN checked_out_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE checkins DROP CONSTRAINT IF EXISTS checkins_room_id_key;

INSERT INTO checkins (id, booking_id, room_id, idempotency_key, created_at)
SELECT '30000000-0000-0000-0000-000000000028', b.id, r.id,
       'seed-demo-checkin-DEMO2028', CURRENT_TIMESTAMP
FROM bookings b
JOIN rooms r ON r.id = '20000000-0000-0000-0000-000000000003'
WHERE b.booking_code = 'DEMO2028'
  AND b.status = 'CHECKED_IN'
  AND NOT EXISTS (SELECT 1 FROM checkins c WHERE c.booking_id = b.id)
  AND r.status = 'AVAILABLE';

UPDATE rooms
SET status = 'OCCUPIED'
WHERE id IN (
  SELECT room_id FROM checkins
  WHERE booking_id = '10000000-0000-0000-0000-000000000003'
);
