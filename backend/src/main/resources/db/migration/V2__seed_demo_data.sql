INSERT INTO bookings (id, booking_code, guest_first_name, guest_last_name, phone_number, room_type, check_in_date, check_out_date, guest_count, status) VALUES
('10000000-0000-0000-0000-000000000001', 'DEMO2026', 'Alex', 'Rivera', '+66000000001', 'Deluxe King', CURRENT_DATE, CURRENT_DATE + 2, 2, 'CONFIRMED'),
('10000000-0000-0000-0000-000000000002', 'DEMO2027', 'Jamie', 'Morgan', '+66000000002', 'Garden Twin', CURRENT_DATE, CURRENT_DATE + 3, 1, 'CONFIRMED'),
('10000000-0000-0000-0000-000000000003', 'DEMO2028', 'Taylor', 'Chen', '+66000000003', 'Deluxe King', CURRENT_DATE, CURRENT_DATE + 1, 2, 'CHECKED_IN');
INSERT INTO rooms (id, room_number, floor_number, room_type, status) VALUES
('20000000-0000-0000-0000-000000000001', '201', 2, 'Deluxe King', 'AVAILABLE'),
('20000000-0000-0000-0000-000000000002', '202', 2, 'Garden Twin', 'AVAILABLE'),
('20000000-0000-0000-0000-000000000003', '301', 3, 'Deluxe King', 'AVAILABLE'),
('20000000-0000-0000-0000-000000000004', '302', 3, 'Garden Twin', 'AVAILABLE');
