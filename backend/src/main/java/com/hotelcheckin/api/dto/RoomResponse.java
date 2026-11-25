package com.hotelcheckin.api.dto;

import com.hotelcheckin.domain.Room;
import java.util.UUID;

public record RoomResponse(UUID id, String roomNumber, int floor, String roomType, String status) {
  public static RoomResponse from(Room room) { return new RoomResponse(room.getId(), room.getRoomNumber(), room.getFloorNumber(), room.getRoomType(), room.getStatus()); }
}
