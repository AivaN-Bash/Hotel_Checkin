package com.hotelcheckin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hotelcheckin.service.MaintenanceState;
import com.hotelcheckin.service.ServiceStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatusController.class)
@TestPropertySource(properties = "app.admin.api-key=expected-secret-value")
class AdminMaintenanceControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private ServiceStatusService statusService;
  @MockBean private MaintenanceState maintenanceState;

  @Test
  void rejectsWrongAdminKey() throws Exception {
    mockMvc.perform(post("/api/v1/admin/maintenance").header("X-Admin-Key", "wrong-secret-value").contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"))
        .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }
}
