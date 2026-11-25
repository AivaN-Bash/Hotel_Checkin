package com.hotelcheckin.api;

import java.time.OffsetDateTime;

public record ErrorResponse(OffsetDateTime timestamp, int status, String code, String message) {}
