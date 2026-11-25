package com.hotelcheckin.api;

import com.hotelcheckin.api.dto.*;
import com.hotelcheckin.repository.RoomRepository;
import com.hotelcheckin.service.AdminKeyVerifier;
import com.hotelcheckin.service.ApiException;
import com.hotelcheckin.service.BookingService;
import com.hotelcheckin.service.CheckinService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class KioskController {
  private final BookingService bookingService;
  private final CheckinService checkinService;
  private final RoomRepository rooms;
  private final AdminKeyVerifier adminKeyVerifier;

  public KioskController(
      BookingService bookingService,
      CheckinService checkinService,
      RoomRepository rooms,
      AdminKeyVerifier adminKeyVerifier) {
    this.bookingService = bookingService;
    this.checkinService = checkinService;
    this.rooms = rooms;
    this.adminKeyVerifier = adminKeyVerifier;
  }

  @PostMapping("/bookings/lookup")
  public BookingResponse lookup(@Valid @RequestBody BookingLookupRequest request) {
    return bookingService.lookup(request);
  }

  @PostMapping("/bookings/checkout-lookup")
  public BookingResponse checkoutLookup(@Valid @RequestBody BookingLookupRequest request) {
    return bookingService.lookupForCheckout(request);
  }

  @PostMapping("/checkouts")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void checkOut(@Valid @RequestBody CheckinRequest request) {
    checkinService.checkOut(request);
  }

  @PostMapping("/checkins")
  public CheckinResponse checkIn(
      @Valid @RequestBody CheckinRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) String key) {
    if (key == null || !key.matches("[A-Za-z0-9._:-]{8,80}"))
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "VALIDATION_ERROR",
          "A valid Idempotency-Key header is required.");
    return checkinService.checkIn(request, key);
  }

  @GetMapping("/checkins/{id}")
  public CheckinResponse getCheckin(@PathVariable UUID id) {
    return checkinService.get(id);
  }

  @GetMapping("/rooms")
  public List<RoomResponse> getRooms(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
    adminKeyVerifier.requireValid(adminKey);
    return rooms.findAllByOrderByFloorNumberAscRoomNumberAsc().stream()
        .map(RoomResponse::from)
        .toList();
  }
}
