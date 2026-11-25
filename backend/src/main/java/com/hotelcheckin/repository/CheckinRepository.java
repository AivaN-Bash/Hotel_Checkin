package com.hotelcheckin.repository;

import com.hotelcheckin.domain.Checkin;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckinRepository extends JpaRepository<Checkin, UUID> {
  Optional<Checkin> findByIdempotencyKey(String idempotencyKey);

  Optional<Checkin> findByBookingId(UUID bookingId);

  @Query("select c from Checkin c join fetch c.room where c.booking.id = :bookingId")
  Optional<Checkin> findWithRoomByBookingId(@Param("bookingId") UUID bookingId);
}
