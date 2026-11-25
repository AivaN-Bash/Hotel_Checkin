package com.hotelcheckin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hotelcheckin.api.dto.BookingResponse;
import com.hotelcheckin.repository.RoomRepository;
import com.hotelcheckin.service.BookingService;
import com.hotelcheckin.service.ApiException;
import com.hotelcheckin.service.CheckinService;
import com.hotelcheckin.service.MaintenanceState;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(KioskController.class)
class KioskControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private BookingService bookingService;
  @MockBean private CheckinService checkinService;
  @MockBean private RoomRepository roomRepository;
  @MockBean private MaintenanceState maintenanceState;

  @BeforeEach
  void operationalModeForRequests() {
    when(maintenanceState.get()).thenReturn(new MaintenanceState.Snapshot(false, null, null));
  }

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

  @Test
  void returnsConsistentUnavailableErrorForDatabaseFailure() throws Exception {
    when(bookingService.lookup(any())).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("hidden db details"));
    mockMvc.perform(post("/api/v1/bookings/lookup").contentType(MediaType.APPLICATION_JSON).content("{\"bookingCode\":\"DEMO2026\"}"))
        .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("SERVICE_UNAVAILABLE")).andExpect(jsonPath("$.message").value("The booking system is temporarily unavailable. Please try again shortly."));
  }
}
