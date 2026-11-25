package com.hotelcheckin.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookingLookupRequest(
    @Size(max = 24) String bookingCode,
    @Size(max = 80) String lastName,
    @Pattern(regexp = "^[+0-9() .-]{7,32}$", message = "Enter a valid phone number")
        String phoneNumber) {
  public boolean hasCode() {
    return bookingCode != null && !bookingCode.isBlank();
  }

  public boolean hasNameAndPhone() {
    return lastName != null && !lastName.isBlank() && phoneNumber != null && !phoneNumber.isBlank();
  }
}
