package com.hotelcheckin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hotelcheckin.api.dto.BookingResponse;
import com.hotelcheckin.repository.RoomRepository;
import com.hotelcheckin.service.BookingService;
import com.hotelcheckin.service.CheckinService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(KioskController.class)
class KioskControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private BookingService bookingService;
  @MockBean private CheckinService checkinService;
  @MockBean private RoomRepository roomRepository;

  @Test
  void returnsTypedBookingDto() throws Exception {
    when(bookingService.lookup(any())).thenReturn(new BookingResponse(UUID.randomUUID(), "Alex Rivera", "Deluxe King", LocalDate.now(), LocalDate.now().plusDays(2), 2));
    mockMvc.perform(post("/api/v1/bookings/lookup").contentType(MediaType.APPLICATION_JSON).content("{\"bookingCode\":\"DEMO2026\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.guestName").value("Alex Rivera"));
  }

  @Test
  void returnsConsistentValidationError() throws Exception {
    mockMvc.perform(post("/api/v1/checkins").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR")).andExpect(jsonPath("$.timestamp").exists());
  }
}
