package com.hotelcheckin.api;

import com.hotelcheckin.api.dto.MaintenanceRequest;
import com.hotelcheckin.api.dto.StatusResponse;
import com.hotelcheckin.service.ApiException;
import com.hotelcheckin.service.MaintenanceState;
import com.hotelcheckin.service.ServiceStatusService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class StatusController {
  private final ServiceStatusService statusService;
  private final MaintenanceState maintenance;
  private final String adminKey;
  public StatusController(ServiceStatusService statusService, MaintenanceState maintenance, @Value("${app.admin.api-key:}") String adminKey) {
    this.statusService = statusService;
    this.maintenance = maintenance;
    this.adminKey = adminKey;
  }
  @GetMapping("/status")
  public StatusResponse status() { return statusService.getStatus(); }
  @GetMapping("/health")
  public Map<String, String> health() { return Map.of("status", "UP"); }
  @PostMapping("/admin/maintenance")
  public Map<String, Object> setMaintenance(@Valid @RequestBody MaintenanceRequest request, @RequestHeader(value = "X-Admin-Key", required = false) String providedKey) {
    if (adminKey.isBlank() || providedKey == null || !constantTimeEquals(adminKey, providedKey)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "A valid admin key is required.");
    }
    var state = maintenance.update(request.enabled(), request.message(), request.estimatedBackAt());
    return Map.of("enabled", state.enabled(), "message", state.message() == null ? "" : state.message(), "estimatedBackAt", state.estimatedBackAt() == null ? "" : state.estimatedBackAt());
  }
  private boolean constantTimeEquals(String expected, String supplied) {
    return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
  }
}
