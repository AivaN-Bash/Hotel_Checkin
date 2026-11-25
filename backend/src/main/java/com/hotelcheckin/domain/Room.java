package com.hotelcheckin.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room {
  @Id private UUID id;

  @Column(name = "room_number", nullable = false, unique = true)
  private String roomNumber;

  @Column(name = "floor_number", nullable = false)
  private int floorNumber;

  @Column(name = "room_type", nullable = false)
  private String roomType;

  @Column(nullable = false)
  private String status;

  protected Room() {}

  public UUID getId() {
    return id;
  }

  public String getRoomNumber() {
    return roomNumber;
  }

  public int getFloorNumber() {
    return floorNumber;
  }

  public String getRoomType() {
    return roomType;
  }

  public String getStatus() {
    return status;
  }

  public void markAssigned() {
    status = "OCCUPIED";
  }

  public void markAvailable() {
    status = "AVAILABLE";
  }
}
