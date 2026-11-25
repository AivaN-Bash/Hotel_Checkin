package com.hotelcheckin.service;

import com.hotelcheckin.api.dto.BookingLookupRequest;
import com.hotelcheckin.api.dto.BookingResponse;
import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
  private final BookingRepository bookings;

  public BookingService(BookingRepository bookings) {
    this.bookings = bookings;
  }

  public BookingResponse lookup(BookingLookupRequest request) {
    return lookup(request, false);
  }

  public BookingResponse lookupForCheckout(BookingLookupRequest request) {
    return lookup(request, true);
  }

  private BookingResponse lookup(BookingLookupRequest request, boolean checkout) {
    boolean byCode = request.hasCode();
    if (byCode == request.hasNameAndPhone()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "VALIDATION_ERROR",
          "Provide either a booking code or last name and phone number.");
    }
    Booking booking =
        (byCode
                ? bookings.findByBookingCodeIgnoreCase(request.bookingCode().trim())
                : bookings.findByGuestLastNameIgnoreCaseAndPhoneNumber(
                    request.lastName().trim(), normalizePhone(request.phoneNumber())))
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "BOOKING_NOT_FOUND",
                        "We couldn't find that booking. Check the details and try again."));
    if (checkout && !"CHECKED_IN".equals(booking.getStatus())) {
      throw new ApiException(
          HttpStatus.CONFLICT, "NOT_CHECKED_IN", "This booking is not currently checked in.");
    }
    if (!checkout && "CHECKED_IN".equals(booking.getStatus())) {
      throw new ApiException(
          HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "This booking has already been checked in.");
    }
    return BookingResponse.from(booking);
  }

  private String normalizePhone(String phone) {
    return phone.replaceAll("[(). -]", "");
  }
}
