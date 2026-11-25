package com.hotelcheckin.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CheckinRequest(@NotNull UUID bookingId) {}
