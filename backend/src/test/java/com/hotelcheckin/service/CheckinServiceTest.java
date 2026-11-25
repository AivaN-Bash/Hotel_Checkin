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

  @Test
  void checksOutAnActiveCheckinEvenWhenTheBookingStatusIsStale() {
    UUID bookingId = UUID.randomUUID();
    Booking booking = mock(Booking.class);
    Checkin checkin = mock(Checkin.class);
    com.hotelcheckin.domain.Room room = mock(com.hotelcheckin.domain.Room.class);
    when(bookings.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getStatus()).thenReturn(Booking.STATUS_CONFIRMED);
    when(checkins.findWithRoomByBookingId(bookingId)).thenReturn(Optional.of(checkin));
    when(checkin.getRoom()).thenReturn(room);

    service.checkOut(new CheckinRequest(bookingId));

    verify(room).markAvailable();
    verify(checkin).markCheckedOut();
    verify(booking).markCheckedOut();
  }

  @Test
  void doesNotAllowCheckedOutBookingsToCheckInAgain() {
    UUID bookingId = UUID.randomUUID();
    Booking booking = mock(Booking.class);
    when(checkins.findByIdempotencyKey("new-checkin-key")).thenReturn(Optional.empty());
    when(bookings.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getStatus()).thenReturn(Booking.STATUS_CHECKED_OUT);
    when(checkins.findByBookingId(bookingId)).thenReturn(Optional.empty());

    ApiException exception =
        assertThrows(
            ApiException.class,
            () -> service.checkIn(new CheckinRequest(bookingId), "new-checkin-key"));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertEquals("BOOKING_NOT_AVAILABLE_FOR_CHECKIN", exception.getCode());
    verifyNoInteractions(rooms);
  }

  @Test
  void returnsNotCheckedInWhenThereIsNoAssignmentForAConfirmedBooking() {
    UUID bookingId = UUID.randomUUID();
    Booking booking = mock(Booking.class);
    when(bookings.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getStatus()).thenReturn(Booking.STATUS_CONFIRMED);
    when(checkins.findWithRoomByBookingId(bookingId)).thenReturn(Optional.empty());

    ApiException exception =
        assertThrows(ApiException.class, () -> service.checkOut(new CheckinRequest(bookingId)));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertEquals("NOT_CHECKED_IN", exception.getCode());
  }
}
