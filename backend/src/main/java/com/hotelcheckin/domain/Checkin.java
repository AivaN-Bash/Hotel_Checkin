package com.hotelcheckin.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "checkins")
public class Checkin {
  @Id private UUID id;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "booking_id", nullable = false, unique = true) private Booking booking;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", nullable = false, unique = true) private Room room;
  @Column(name = "idempotency_key", nullable = false, unique = true, length = 80) private String idempotencyKey;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

  protected Checkin() {}
  public Checkin(Booking booking, Room room, String idempotencyKey) {
    this.id = UUID.randomUUID();
    this.booking = booking;
    this.room = room;
    this.idempotencyKey = idempotencyKey;
    this.createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
  }
  public UUID getId() { return id; }
  public Booking getBooking() { return booking; }
  public Room getRoom() { return room; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
