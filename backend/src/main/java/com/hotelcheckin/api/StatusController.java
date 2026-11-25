package com.hotelcheckin.api;

import com.hotelcheckin.api.dto.MaintenanceRequest;
import com.hotelcheckin.api.dto.StatusResponse;
import com.hotelcheckin.service.AdminKeyVerifier;
import com.hotelcheckin.service.MaintenanceState;
import com.hotelcheckin.service.ServiceStatusService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class StatusController {
  private final ServiceStatusService statusService;
  private final MaintenanceState maintenance;
  private final AdminKeyVerifier adminKeyVerifier;

  public StatusController(
      ServiceStatusService statusService,
      MaintenanceState maintenance,
      AdminKeyVerifier adminKeyVerifier) {
    this.statusService = statusService;
    this.maintenance = maintenance;
    this.adminKeyVerifier = adminKeyVerifier;
  }

  @GetMapping("/status")
  public StatusResponse status() {
    return statusService.getStatus();
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP");
  }

  @PostMapping("/admin/maintenance")
  public Map<String, Object> setMaintenance(
      @Valid @RequestBody MaintenanceRequest request,
      @RequestHeader(value = "X-Admin-Key", required = false) String providedKey) {
    adminKeyVerifier.requireValid(providedKey);
    var state = maintenance.update(request.enabled(), request.message(), request.estimatedBackAt());
    return Map.of(
        "enabled",
        state.enabled(),
        "message",
        state.message() == null ? "" : state.message(),
        "estimatedBackAt",
        state.estimatedBackAt() == null ? "" : state.estimatedBackAt());
  }
}
