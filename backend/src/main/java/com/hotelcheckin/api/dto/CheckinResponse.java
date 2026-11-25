package com.hotelcheckin.api.dto;

import com.hotelcheckin.domain.Checkin;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckinResponse(
    UUID id,
    String guestName,
    String roomType,
    String roomNumber,
    int floor,
    String digitalKey,
    OffsetDateTime createdAt) {
  public static CheckinResponse from(Checkin checkin) {
    var booking = checkin.getBooking();
    var room = checkin.getRoom();
    return new CheckinResponse(
        checkin.getId(),
        booking.getGuestFirstName() + " " + booking.getGuestLastName(),
        room.getRoomType(),
        room.getRoomNumber(),
        room.getFloorNumber(),
        "DEMO-KEY-" + checkin.getId(),
        checkin.getCreatedAt());
  }
}
