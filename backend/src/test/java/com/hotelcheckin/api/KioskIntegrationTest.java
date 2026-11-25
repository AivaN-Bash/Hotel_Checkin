package com.hotelcheckin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class KioskIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Test
  void seedsAnActiveDemoStayForCheckout() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/bookings/checkout-lookup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingCode\":\"DEMO2028\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.guestName").value("Taylor Chen"));
  }

  @Test
  void assignsOnlyOneRoomAcrossIdempotentRetries() throws Exception {
    MvcResult first =
        mockMvc
            .perform(
                post("/api/v1/checkins")
                    .header("Idempotency-Key", "integration-retry-key")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"bookingId\":\"10000000-0000-0000-0000-000000000001\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roomNumber").value("201"))
            .andReturn();
    String checkinId =
        com.jayway.jsonpath.JsonPath.read(first.getResponse().getContentAsString(), "$.id");

    mockMvc
        .perform(
            post("/api/v1/checkins")
                .header("Idempotency-Key", "integration-retry-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingId\":\"10000000-0000-0000-0000-000000000001\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(checkinId))
        .andExpect(jsonPath("$.roomNumber").value("201"));

    mockMvc
        .perform(get("/api/v1/checkins/{id}", checkinId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.digitalKey").value("DEMO-KEY-" + checkinId));

    mockMvc
        .perform(
            post("/api/v1/checkins")
                .header("Idempotency-Key", "another-retry-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingId\":\"10000000-0000-0000-0000-000000000001\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ALREADY_CHECKED_IN"));

    mockMvc
        .perform(
            post("/api/v1/bookings/checkout-lookup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingCode\":\"DEMO2026\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.guestName").value("Alex Rivera"));

    mockMvc
        .perform(
            post("/api/v1/checkouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingId\":\"10000000-0000-0000-0000-000000000001\"}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/v1/bookings/checkout-lookup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookingCode\":\"DEMO2026\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("NOT_CHECKED_IN"));

    mockMvc
        .perform(get("/api/v1/rooms"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("ADMIN_NOT_CONFIGURED"));
  }
}
