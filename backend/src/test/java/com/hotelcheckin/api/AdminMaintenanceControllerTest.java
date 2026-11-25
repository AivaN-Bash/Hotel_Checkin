package com.hotelcheckin.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hotelcheckin.service.AdminKeyVerifier;
import com.hotelcheckin.service.MaintenanceState;
import com.hotelcheckin.service.ServiceStatusService;
import com.hotelcheckin.service.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatusController.class)
@TestPropertySource(properties = "app.admin.api-key=expected-secret-value-1234567890")
class AdminMaintenanceControllerTest {
  private static final String ADMIN_KEY_HEADER = "X-Admin-Key";
  private static final String WRONG_ADMIN_KEY = "wrong-secret-value";
  private static final String MAINTENANCE_REQUEST = "{\"enabled\":true}";

  @Autowired private MockMvc mockMvc;
  @MockBean private ServiceStatusService statusService;
  @MockBean private MaintenanceState maintenanceState;
  @MockBean private AdminKeyVerifier adminKeyVerifier;

  @Test
  void rejectsWrongAdminKey() throws Exception {
    doThrow(
            new ApiException(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "A valid admin key is required."))
        .when(adminKeyVerifier)
        .requireValid(WRONG_ADMIN_KEY);

    mockMvc
        .perform(
            post("/api/v1/admin/maintenance")
                .header(ADMIN_KEY_HEADER, WRONG_ADMIN_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAINTENANCE_REQUEST))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    verify(adminKeyVerifier).requireValid(WRONG_ADMIN_KEY);
  }
}
