package com.hotelcheckin.service;

import com.hotelcheckin.api.dto.BookingLookupRequest;
import com.hotelcheckin.api.dto.BookingResponse;
import com.hotelcheckin.domain.Booking;
import com.hotelcheckin.domain.Checkin;
import com.hotelcheckin.domain.Room;
import com.hotelcheckin.repository.BookingRepository;
import com.hotelcheckin.repository.CheckinRepository;
import com.hotelcheckin.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
  private static final Set<String> REUSABLE_CHECKIN_DEMO_CODES = Set.of("DEMO2026", "DEMO2027");
  private static final String REUSABLE_CHECKOUT_DEMO_CODE = "DEMO2028";

  private final BookingRepository bookings;
  private final CheckinRepository checkins;
  private final RoomRepository rooms;

  public BookingService(
      BookingRepository bookings, CheckinRepository checkins, RoomRepository rooms) {
    this.bookings = bookings;
    this.checkins = checkins;
    this.rooms = rooms;
  }

  @Transactional
  public BookingResponse lookup(BookingLookupRequest request) {
    return lookup(request, LookupPurpose.CHECK_IN);
  }

  @Transactional
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
    if (isDemoBooking(booking)) {
      booking =
          bookings
              .findLockedById(booking.getId())
              .orElseThrow(
                  () ->
                      new ApiException(
                          HttpStatus.NOT_FOUND,
                          "BOOKING_NOT_FOUND",
                          "We couldn't find that booking. Check the details and try again."));
    }

    Optional<Checkin> checkin = checkins.findByBookingId(booking.getId());
    if (purpose == LookupPurpose.CHECK_IN
        && isReusableCheckinDemo(booking)
        && isCompletedStay(booking, checkin)) {
      checkins.delete(checkin.orElseThrow());
      checkins.flush();
      booking.markConfirmed();
      checkin = Optional.empty();
    } else if (purpose == LookupPurpose.CHECK_OUT
        && isReusableCheckoutDemo(booking)
        && isCompletedStay(booking, checkin)) {
      checkins.delete(checkin.orElseThrow());
      checkins.flush();
      Room room =
          rooms
              .findAvailableForUpdate(booking.getRoomType())
              .orElseThrow(
                  () ->
                      new ApiException(
                          HttpStatus.CONFLICT,
                          "NO_ROOM_AVAILABLE",
                          "No suitable room is available. Please visit the front desk."));
      room.markAssigned();
      booking.markCheckedIn();
      checkin =
          Optional.of(checkins.save(new Checkin(booking, room, "demo-reset-" + UUID.randomUUID())));
    }

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

  private boolean isDemoBooking(Booking booking) {
    return isReusableCheckinDemo(booking) || isReusableCheckoutDemo(booking);
  }

  private boolean isReusableCheckinDemo(Booking booking) {
    return REUSABLE_CHECKIN_DEMO_CODES.contains(normalizeBookingCode(booking));
  }

  private boolean isReusableCheckoutDemo(Booking booking) {
    return REUSABLE_CHECKOUT_DEMO_CODE.equals(normalizeBookingCode(booking));
  }

  private String normalizeBookingCode(Booking booking) {
    return booking.getBookingCode() == null
        ? ""
        : booking.getBookingCode().toUpperCase(Locale.ROOT);
  }

  private boolean isCompletedStay(Booking booking, Optional<Checkin> checkin) {
    return Booking.STATUS_CHECKED_OUT.equals(booking.getStatus())
        && checkin.isPresent()
        && checkin.get().getCheckedOutAt() != null;
  }

  private String normalizePhone(String phone) {
    return phone.replaceAll("[(). -]", "");
  }

  private enum LookupPurpose {
    CHECK_IN,
    CHECK_OUT
  }
}
