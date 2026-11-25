CREATE TABLE bookings (
  id UUID PRIMARY KEY,
  booking_code VARCHAR(24) NOT NULL UNIQUE,
  guest_first_name VARCHAR(80) NOT NULL,
  guest_last_name VARCHAR(80) NOT NULL,
  phone_number VARCHAR(32) NOT NULL,
  room_type VARCHAR(40) NOT NULL,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  guest_count INTEGER NOT NULL CHECK (guest_count > 0),
  status VARCHAR(24) NOT NULL DEFAULT 'CONFIRMED'
);
CREATE TABLE rooms (
  id UUID PRIMARY KEY,
  room_number VARCHAR(12) NOT NULL UNIQUE,
  floor_number INTEGER NOT NULL,
  room_type VARCHAR(40) NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'AVAILABLE'
);
CREATE TABLE checkins (
  id UUID PRIMARY KEY,
  booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id),
  room_id UUID NOT NULL UNIQUE REFERENCES rooms(id),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_bookings_lookup ON bookings (booking_code, guest_last_name);
CREATE INDEX idx_rooms_available ON rooms (status, room_type);
