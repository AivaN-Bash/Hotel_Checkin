package com.hotelcheckin.repository;

import com.hotelcheckin.domain.Booking;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
  Optional<Booking> findByBookingCodeIgnoreCase(String bookingCode);
  Optional<Booking> findByGuestLastNameIgnoreCaseAndPhoneNumber(String lastName, String phoneNumber);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Booking b where b.id = :id")
  Optional<Booking> findLockedById(@Param("id") UUID id);
}
