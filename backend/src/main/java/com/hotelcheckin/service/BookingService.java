package com.hotelcheckin.service;

import com.hotelcheckin.api.dto.BookingLookupRequest;
import com.hotelcheckin.api.dto.BookingResponse;
import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.repository.BookingRepository;
import com.hotelcheckin.repository.CheckinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
  private final BookingRepository bookings;
  private final CheckinRepository checkins;

  public BookingService(BookingRepository bookings, CheckinRepository checkins) {
    this.bookings = bookings;
    this.checkins = checkins;
  }

  public BookingResponse lookup(BookingLookupRequest request) {
    return lookup(request, LookupPurpose.CHECK_IN);
  }

  public BookingResponse lookupForCheckout(BookingLookupRequest request) {
    return lookup(request, LookupPurpose.CHECK_OUT);
  }

  private BookingResponse lookup(BookingLookupRequest request, LookupPurpose purpose) {
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
    var checkin = checkins.findByBookingId(booking.getId());
    boolean activelyCheckedIn = checkin.isPresent() && checkin.get().getCheckedOutAt() == null;
    if (purpose == LookupPurpose.CHECK_OUT && !activelyCheckedIn) {
      throw new ApiException(
          HttpStatus.CONFLICT, "NOT_CHECKED_IN", "This booking is not currently checked in.");
    }
    if (purpose == LookupPurpose.CHECK_IN) {
      if (activelyCheckedIn) {
        throw new ApiException(
            HttpStatus.CONFLICT,
            "ALREADY_CHECKED_IN",
            "This booking is already checked in. Start a check-out session to continue.");
      }
      if (!Booking.STATUS_CONFIRMED.equals(booking.getStatus()) || checkin.isPresent()) {
        throw new ApiException(
            HttpStatus.CONFLICT,
            "BOOKING_NOT_AVAILABLE_FOR_CHECKIN",
            "This booking is not available for check-in. Please visit the front desk.");
      }
    }
    return BookingResponse.from(booking);
  }

  private String normalizePhone(String phone) {
    return phone.replaceAll("[(). -]", "");
  }

  private enum LookupPurpose {
    CHECK_IN,
    CHECK_OUT
  }
}
