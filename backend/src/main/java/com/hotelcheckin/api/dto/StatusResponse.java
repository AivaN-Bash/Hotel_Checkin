package com.hotelcheckin.api.dto;

import java.time.OffsetDateTime;

public record StatusResponse(
    String status,
    Components components,
    String message,
    OffsetDateTime estimatedBackAt,
    OffsetDateTime checkedAt) {
  public record Components(String api, String database) {}
}
