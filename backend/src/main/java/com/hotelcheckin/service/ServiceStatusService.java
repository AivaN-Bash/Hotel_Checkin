package com.hotelcheckin.service;

import com.hotelcheckin.api.dto.StatusResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceStatusService {
  private static final int DATABASE_CHECK_TIMEOUT_SECONDS = 2;
  private final JdbcTemplate jdbc;
  private final MaintenanceState maintenance;

  public ServiceStatusService(JdbcTemplate jdbc, MaintenanceState maintenance) {
    this.jdbc = jdbc;
    this.maintenance = maintenance;
    this.jdbc.setQueryTimeout(DATABASE_CHECK_TIMEOUT_SECONDS);
  }

  public StatusResponse getStatus() {
    String database = "UP";
    try {
      jdbc.queryForObject("SELECT 1", Integer.class);
    } catch (RuntimeException exception) {
      database = "DOWN";
    }
    var current = maintenance.get();
    String status =
        current.enabled() ? "MAINTENANCE" : database.equals("DOWN") ? "DEGRADED" : "OPERATIONAL";
    String message =
        current.enabled()
            ? current.message()
            : database.equals("DOWN")
                ? "The booking system is temporarily unavailable. Please visit the front desk for assistance."
                : "All kiosk services are operational.";
    return new StatusResponse(
        status,
        new StatusResponse.Components("UP", database),
        message,
        current.enabled() ? current.estimatedBackAt() : null,
        OffsetDateTime.now(ZoneOffset.UTC));
  }
}
