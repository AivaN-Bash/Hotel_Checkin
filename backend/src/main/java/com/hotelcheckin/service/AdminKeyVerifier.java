package com.hotelcheckin.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AdminKeyVerifier {
  private static final int MINIMUM_KEY_LENGTH = 32;
  private final String expectedKey;

  public AdminKeyVerifier(@Value("${app.admin.api-key:}") String expectedKey) {
    this.expectedKey = expectedKey;
  }

  public void requireValid(String providedKey) {
    if (expectedKey.length() < MINIMUM_KEY_LENGTH) {
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "ADMIN_NOT_CONFIGURED",
          "Admin access is not configured.");
    }
    if (providedKey == null
        || !MessageDigest.isEqual(
            expectedKey.getBytes(StandardCharsets.UTF_8),
            providedKey.getBytes(StandardCharsets.UTF_8))) {
      throw new ApiException(
          HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "A valid admin key is required.");
    }
  }
}
