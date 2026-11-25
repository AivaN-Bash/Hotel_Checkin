package com.hotelcheckin.repository;

import com.hotelcheckin.domain.Checkin;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckinRepository extends JpaRepository<Checkin, UUID> {
  Optional<Checkin> findByIdempotencyKey(String idempotencyKey);
  Optional<Checkin> findByBookingId(UUID bookingId);
}
