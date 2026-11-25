package com.hotelcheckin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.repository.BookingRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BookingServiceTest {
  private final BookingRepository repository = mock(BookingRepository.class);
  private final BookingService service = new BookingService(repository);

  @Test
  void returnsNotFoundWithoutLeakingLookupDetails() {
    when(repository.findByBookingCodeIgnoreCase("MISSING")).thenReturn(Optional.empty());
    ApiException exception = assertThrows(ApiException.class, () -> service.lookup(new com.hotelcheckin.api.dto.BookingLookupRequest("MISSING", null, null)));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertEquals("BOOKING_NOT_FOUND", exception.getCode());
  }

  @Test
  void rejectsAmbiguousLookupInput() {
    ApiException exception = assertThrows(ApiException.class, () -> service.lookup(new com.hotelcheckin.api.dto.BookingLookupRequest("CODE", "Name", "+66000000001")));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
  }

  @Test
  void trimsAndNormalizesPhoneLookup() {
    Booking booking = mock(Booking.class);
    when(repository.findByGuestLastNameIgnoreCaseAndPhoneNumber("Rivera", "+66000000001")).thenReturn(Optional.of(booking));
    when(booking.getStatus()).thenReturn("CONFIRMED");
    when(booking.getGuestFirstName()).thenReturn("Alex");
    when(booking.getGuestLastName()).thenReturn("Rivera");
    when(booking.getRoomType()).thenReturn("Deluxe King");
    when(booking.getCheckInDate()).thenReturn(java.time.LocalDate.now());
    when(booking.getCheckOutDate()).thenReturn(java.time.LocalDate.now().plusDays(2));
    when(booking.getGuestCount()).thenReturn(2);
    assertEquals("Alex Rivera", service.lookup(new com.hotelcheckin.api.dto.BookingLookupRequest(null, " Rivera ", "+66 0000-00001")).guestName());
  }
}
