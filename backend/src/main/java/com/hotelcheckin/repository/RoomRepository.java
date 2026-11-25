package com.hotelcheckin.repository;

import com.hotelcheckin.domain.Room;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, UUID> {
  @Query(
      value =
          "select * from rooms where status = 'AVAILABLE' and room_type = :roomType order by floor_number, room_number limit 1 for update skip locked",
      nativeQuery = true)
  Optional<Room> findAvailableForUpdate(@Param("roomType") String roomType);

  List<Room> findAllByOrderByFloorNumberAscRoomNumberAsc();
}
