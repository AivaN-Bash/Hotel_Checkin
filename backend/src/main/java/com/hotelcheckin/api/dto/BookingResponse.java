package com.hotelcheckin.api.dto;

import com.hotelcheckin.domain.Booking;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(UUID id, String guestName, String roomType, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) {
  public static BookingResponse from(Booking booking) {
    return new BookingResponse(booking.getId(), booking.getGuestFirstName() + " " + booking.getGuestLastName(), booking.getRoomType(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getGuestCount());
  }
}
