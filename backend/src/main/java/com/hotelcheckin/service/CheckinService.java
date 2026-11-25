package com.hotelcheckin.service;

import com.hotelcheckin.api.dto.CheckinRequest;
import com.hotelcheckin.api.dto.CheckinResponse;
import com.hotelcheckin.domain.Checkin;
import com.hotelcheckin.repository.BookingRepository;
import com.hotelcheckin.repository.CheckinRepository;
import com.hotelcheckin.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckinService {
  private final BookingRepository bookings;
  private final RoomRepository rooms;
  private final CheckinRepository checkins;

  public CheckinService(
      BookingRepository bookings, RoomRepository rooms, CheckinRepository checkins) {
    this.bookings = bookings;
    this.rooms = rooms;
    this.checkins = checkins;
  }

  @Transactional
  public CheckinResponse checkIn(CheckinRequest request, String idempotencyKey) {
    var priorRequest = checkins.findByIdempotencyKey(idempotencyKey);
    if (priorRequest.isPresent()) {
      if (!priorRequest.get().getBooking().getId().equals(request.bookingId())) {
        throw new ApiException(
            HttpStatus.CONFLICT,
            "IDEMPOTENCY_KEY_REUSED",
            "This request key is already associated with another booking.");
      }
      return CheckinResponse.from(priorRequest.get());
    }
    var booking =
        bookings
            .findLockedById(request.bookingId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found."));
    var completed = checkins.findByBookingId(booking.getId());
    if (completed.isPresent()) {
      if (completed.get().getIdempotencyKey().equals(idempotencyKey))
        return CheckinResponse.from(completed.get());
      throw new ApiException(
          HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "This booking has already been checked in.");
    }
    if ("CHECKED_IN".equals(booking.getStatus()))
      throw new ApiException(
          HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "This booking has already been checked in.");
    var room =
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
    try {
      return CheckinResponse.from(checkins.save(new Checkin(booking, room, idempotencyKey)));
    } catch (DataIntegrityViolationException exception) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "CHECKIN_CONFLICT",
          "This check-in was already completed. Please refresh your booking.");
    }
  }

  @Transactional
  public void checkOut(CheckinRequest request) {
    var booking =
        bookings
            .findLockedById(request.bookingId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found."));
    if (!"CHECKED_IN".equals(booking.getStatus()))
      throw new ApiException(
          HttpStatus.CONFLICT, "NOT_CHECKED_IN", "This booking is not currently checked in.");
    var checkin =
        checkins
            .findWithRoomByBookingId(booking.getId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.CONFLICT,
                        "CHECKIN_RECORD_MISSING",
                        "No active room assignment was found."));
    checkin.getRoom().markAvailable();
    checkin.markCheckedOut();
    booking.markCheckedOut();
  }

  @Transactional
  public CheckinResponse get(UUID id) {
    return checkins
        .findById(id)
        .map(CheckinResponse::from)
        .orElseThrow(
            () ->
                new ApiException(HttpStatus.NOT_FOUND, "CHECKIN_NOT_FOUND", "Check-in not found."));
  }
}
