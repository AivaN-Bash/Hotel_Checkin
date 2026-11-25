package com.hotelcheckin.service;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceState {
  private final AtomicReference<Snapshot> value;
  public MaintenanceState(@Value("${app.maintenance.enabled:false}") boolean initiallyEnabled) {
    this.value = new AtomicReference<>(new Snapshot(initiallyEnabled, initiallyEnabled ? "The kiosk is temporarily unavailable while we complete scheduled maintenance." : null, null));
  }
  public Snapshot get() { return value.get(); }
  public Snapshot update(boolean enabled, String message, OffsetDateTime estimatedBackAt) {
    Snapshot next = enabled
        ? new Snapshot(true, message == null || message.isBlank() ? "The kiosk is temporarily unavailable while we complete scheduled maintenance." : message.trim(), estimatedBackAt)
        : new Snapshot(false, null, null);
    value.set(next);
    return next;
  }
  public record Snapshot(boolean enabled, String message, OffsetDateTime estimatedBackAt) {}
}
