package com.hotelcheckin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hotelcheckin.api.dto.CheckinRequest;
import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.domain.Checkin;
import com.hotelcheckin.repository.BookingRepository;
import com.hotelcheckin.repository.CheckinRepository;
import com.hotelcheckin.repository.RoomRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CheckinServiceTest {
  private final BookingRepository bookings = mock(BookingRepository.class);
  private final RoomRepository rooms = mock(RoomRepository.class);
  private final CheckinRepository checkins = mock(CheckinRepository.class);
  private final CheckinService service = new CheckinService(bookings, rooms, checkins);

  @Test
  void doesNotAllowAnIdempotencyKeyToBeReusedForAnotherBooking() {
    UUID originalBookingId = UUID.randomUUID();
    Checkin previous = mock(Checkin.class);
    Booking booking = mock(Booking.class);
    when(checkins.findByIdempotencyKey("retry-key-123")).thenReturn(Optional.of(previous));
    when(previous.getBooking()).thenReturn(booking);
    when(booking.getId()).thenReturn(originalBookingId);

    ApiException exception =
        assertThrows(
            ApiException.class,
            () -> service.checkIn(new CheckinRequest(UUID.randomUUID()), "retry-key-123"));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertEquals("IDEMPOTENCY_KEY_REUSED", exception.getCode());
    verifyNoInteractions(rooms);
  }

  @Test
  void returnsExistingCheckinWhenAConcurrentRetryFindsItsBookingLock() {
    UUID bookingId = UUID.randomUUID();
    Booking booking = mock(Booking.class);
    Checkin previous = mock(Checkin.class);
    when(checkins.findByIdempotencyKey("retry-key-123")).thenReturn(Optional.empty());
    when(bookings.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(checkins.findByBookingId(bookingId)).thenReturn(Optional.of(previous));
    when(previous.getIdempotencyKey()).thenReturn("retry-key-123");
    when(previous.getBooking()).thenReturn(booking);
    when(previous.getRoom()).thenReturn(mock(com.hotelcheckin.domain.Room.class));
    when(booking.getGuestFirstName()).thenReturn("Alex");
    when(booking.getGuestLastName()).thenReturn("Rivera");

    assertEquals(
        "Alex Rivera", service.checkIn(new CheckinRequest(bookingId), "retry-key-123").guestName());
    verifyNoInteractions(rooms);
  }
}
