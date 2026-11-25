package com.hotelcheckin.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record MaintenanceRequest(
    @NotNull Boolean enabled, @Size(max = 240) String message, OffsetDateTime estimatedBackAt) {}
