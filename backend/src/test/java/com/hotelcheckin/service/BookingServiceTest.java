package com.hotelcheckin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.repository.BookingRepository;
import com.hotelcheckin.repository.CheckinRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BookingServiceTest {
  private final BookingRepository repository = mock(BookingRepository.class);
  private final CheckinRepository checkins = mock(CheckinRepository.class);
  private final BookingService service = new BookingService(repository, checkins);

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
  void rejectsCheckedOutBookingsForCheckin() {
    Booking booking = mock(Booking.class);
    when(repository.findByBookingCodeIgnoreCase("DEMO2026")).thenReturn(Optional.of(booking));
    when(booking.getStatus()).thenReturn(Booking.STATUS_CHECKED_OUT);

    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.lookup(
                    new com.hotelcheckin.api.dto.BookingLookupRequest("DEMO2026", null, null)));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    assertEquals("BOOKING_NOT_AVAILABLE_FOR_CHECKIN", exception.getCode());
  }

  @Test
  void rejectsAConfirmedBookingThatAlreadyHasACheckinRecord() {
    Booking booking = mock(Booking.class);
    when(repository.findByBookingCodeIgnoreCase("DEMO2026")).thenReturn(Optional.of(booking));
    when(booking.getId()).thenReturn(java.util.UUID.randomUUID());
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
}
