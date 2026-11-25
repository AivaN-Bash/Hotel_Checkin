package com.hotelcheckin.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
  public static final String STATUS_CONFIRMED = "CONFIRMED";
  public static final String STATUS_CHECKED_IN = "CHECKED_IN";
  public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

  @Id private UUID id;

  @Column(name = "booking_code", nullable = false, unique = true)
  private String bookingCode;

  @Column(name = "guest_first_name", nullable = false)
  private String guestFirstName;

  @Column(name = "guest_last_name", nullable = false)
  private String guestLastName;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Column(name = "room_type", nullable = false)
  private String roomType;

  @Column(name = "check_in_date", nullable = false)
  private LocalDate checkInDate;

  @Column(name = "check_out_date", nullable = false)
  private LocalDate checkOutDate;

  @Column(name = "guest_count", nullable = false)
  private int guestCount;

  @Column(nullable = false)
  private String status;

  protected Booking() {}

  public UUID getId() {
    return id;
  }

  public String getBookingCode() {
    return bookingCode;
  }

  public String getGuestFirstName() {
    return guestFirstName;
  }

  public String getGuestLastName() {
    return guestLastName;
  }

  public String getRoomType() {
    return roomType;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public LocalDate getCheckOutDate() {
    return checkOutDate;
  }

  public int getGuestCount() {
    return guestCount;
  }

  public String getStatus() {
    return status;
  }

  public void markCheckedIn() {
    status = STATUS_CHECKED_IN;
  }

  public void markConfirmed() {
    status = STATUS_CONFIRMED;
  }

  public void markCheckedOut() {
    status = STATUS_CHECKED_OUT;
  }
}
