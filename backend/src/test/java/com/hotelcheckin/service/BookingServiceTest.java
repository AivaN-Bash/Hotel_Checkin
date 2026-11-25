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
}
