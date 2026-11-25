package com.hotelcheckin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.domain.Checkin;
import com.hotelcheckin.domain.Room;
import com.hotelcheckin.repository.BookingRepository;
import com.hotelcheckin.repository.CheckinRepository;
import com.hotelcheckin.repository.RoomRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BookingServiceTest {
  private final BookingRepository repository = mock(BookingRepository.class);
  private final CheckinRepository checkins = mock(CheckinRepository.class);
  private final RoomRepository rooms = mock(RoomRepository.class);
  private final BookingService service = new BookingService(repository, checkins, rooms);

  @Test
  void returnsNotFoundWithoutLeakingLookupDetails() {
    when(repository.findByBookingCodeIgnoreCase("MISSING")).thenReturn(Optional.empty());
    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.lookup(
                    new com.hotelcheckin.api.dto.BookingLookupRequest("MISSING", null, null)));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertEquals("BOOKING_NOT_FOUND", exception.getCode());
  }

  @Test
  void rejectsAmbiguousLookupInput() {
    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.lookup(
                    new com.hotelcheckin.api.dto.BookingLookupRequest(
                        "CODE", "Name", "+66000000001")));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
  }

  @Test
  void trimsAndNormalizesPhoneLookup() {
    Booking booking = mock(Booking.class);
    when(repository.findByGuestLastNameIgnoreCaseAndPhoneNumber("Rivera", "+66000000001"))
        .thenReturn(Optional.of(booking));
    when(booking.getStatus()).thenReturn("CONFIRMED");
    when(booking.getGuestFirstName()).thenReturn("Alex");
    when(booking.getGuestLastName()).thenReturn("Rivera");
    when(booking.getRoomType()).thenReturn("Deluxe King");
    when(booking.getCheckInDate()).thenReturn(java.time.LocalDate.now());
    when(booking.getCheckOutDate()).thenReturn(java.time.LocalDate.now().plusDays(2));
    when(booking.getGuestCount()).thenReturn(2);
    assertEquals(
        "Alex Rivera",
        service
            .lookup(
                new com.hotelcheckin.api.dto.BookingLookupRequest(
                    null, " Rivera ", "+66 0000-00001"))
            .guestName());
  }

  @Test
  void restoresACompletedDemoStayForCheckin() {
    Booking booking = mock(Booking.class);
    Checkin completedStay = mock(Checkin.class);
    UUID bookingId = UUID.randomUUID();
    when(repository.findByBookingCodeIgnoreCase("DEMO2026")).thenReturn(Optional.of(booking));
    when(repository.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getBookingCode()).thenReturn("DEMO2026");
    when(booking.getStatus()).thenReturn(Booking.STATUS_CHECKED_OUT, Booking.STATUS_CONFIRMED);
    when(checkins.findByBookingId(bookingId)).thenReturn(Optional.of(completedStay));
    when(completedStay.getCheckedOutAt()).thenReturn(java.time.OffsetDateTime.now());
    when(booking.getGuestFirstName()).thenReturn("Alex");
    when(booking.getGuestLastName()).thenReturn("Rivera");

    assertEquals(
        "Alex Rivera",
        service
            .lookup(new com.hotelcheckin.api.dto.BookingLookupRequest("DEMO2026", null, null))
            .guestName());
    verify(checkins).delete(completedStay);
    verify(checkins).flush();
    verify(booking).markConfirmed();
  }

  @Test
  void rejectsAConfirmedBookingThatAlreadyHasACheckinRecord() {
    Booking booking = mock(Booking.class);
    UUID bookingId = UUID.randomUUID();
    when(repository.findByBookingCodeIgnoreCase("DEMO2026")).thenReturn(Optional.of(booking));
    when(repository.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getBookingCode()).thenReturn("DEMO2026");
    when(booking.getStatus()).thenReturn(Booking.STATUS_CONFIRMED);
    when(checkins.findByBookingId(booking.getId()))
        .thenReturn(Optional.of(mock(com.hotelcheckin.domain.Checkin.class)));

    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.lookup(
                    new com.hotelcheckin.api.dto.BookingLookupRequest("DEMO2026", null, null)));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertEquals("ALREADY_CHECKED_IN", exception.getCode());
  }

  @Test
  void allowsCheckoutWhenAnActiveCheckinRecordExistsDespiteStaleBookingStatus() {
    Booking booking = mock(Booking.class);
    com.hotelcheckin.domain.Checkin checkin = mock(com.hotelcheckin.domain.Checkin.class);
    when(repository.findByBookingCodeIgnoreCase("DEMO2028")).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(java.util.UUID.randomUUID());
    when(repository.findLockedById(booking.getId())).thenReturn(Optional.of(booking));
    when(booking.getBookingCode()).thenReturn("DEMO2028");
    when(booking.getStatus()).thenReturn(Booking.STATUS_CONFIRMED);
    when(checkins.findByBookingId(booking.getId())).thenReturn(Optional.of(checkin));
    when(booking.getGuestFirstName()).thenReturn("Taylor");
    when(booking.getGuestLastName()).thenReturn("Chen");

    assertEquals(
        "Taylor Chen",
        service
            .lookupForCheckout(
                new com.hotelcheckin.api.dto.BookingLookupRequest("DEMO2028", null, null))
            .guestName());
  }

  @Test
  void restoresCompletedDemoCheckoutWithAnAvailableRoom() {
    Booking booking = mock(Booking.class);
    Checkin completedStay = mock(Checkin.class);
    Room room = mock(Room.class);
    UUID bookingId = UUID.randomUUID();
    when(repository.findByBookingCodeIgnoreCase("DEMO2028")).thenReturn(Optional.of(booking));
    when(repository.findLockedById(bookingId)).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getBookingCode()).thenReturn("DEMO2028");
    when(booking.getStatus()).thenReturn(Booking.STATUS_CHECKED_OUT);
    when(booking.getRoomType()).thenReturn("Deluxe King");
    when(booking.getGuestFirstName()).thenReturn("Taylor");
    when(booking.getGuestLastName()).thenReturn("Chen");
    when(checkins.findByBookingId(bookingId)).thenReturn(Optional.of(completedStay));
    when(completedStay.getCheckedOutAt()).thenReturn(java.time.OffsetDateTime.now());
    when(rooms.findAvailableForUpdate("Deluxe King")).thenReturn(Optional.of(room));
    when(checkins.save(org.mockito.ArgumentMatchers.any(Checkin.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    assertEquals(
        "Taylor Chen",
        service
            .lookupForCheckout(
                new com.hotelcheckin.api.dto.BookingLookupRequest("DEMO2028", null, null))
            .guestName());
    verify(checkins).delete(completedStay);
    verify(checkins).flush();
    verify(room).markAssigned();
    verify(booking).markCheckedIn();
    verify(checkins).save(org.mockito.ArgumentMatchers.any(Checkin.class));
  }

  @Test
  void doesNotRestoreCompletedRealBookings() {
    Booking booking = mock(Booking.class);
    Checkin completedStay = mock(Checkin.class);
    UUID bookingId = UUID.randomUUID();
    when(repository.findByBookingCodeIgnoreCase("GUEST100")).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getBookingCode()).thenReturn("GUEST100");
    when(booking.getStatus()).thenReturn(Booking.STATUS_CHECKED_OUT);
    when(checkins.findByBookingId(bookingId)).thenReturn(Optional.of(completedStay));
    when(completedStay.getCheckedOutAt()).thenReturn(java.time.OffsetDateTime.now());

    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.lookup(
                    new com.hotelcheckin.api.dto.BookingLookupRequest("GUEST100", null, null)));

    assertEquals("BOOKING_NOT_AVAILABLE_FOR_CHECKIN", exception.getCode());
    verify(checkins, never()).delete(org.mockito.ArgumentMatchers.any(Checkin.class));
    verify(booking, never()).markConfirmed();
  }
}
