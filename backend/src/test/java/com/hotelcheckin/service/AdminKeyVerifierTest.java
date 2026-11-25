package com.hotelcheckin.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminKeyVerifierTest {
  @Test
  void failsClosedWhenTheAdminKeyIsMissingOrTooShort() {
    ApiException exception =
        assertThrows(
            ApiException.class, () -> new AdminKeyVerifier("short-key").requireValid("short-key"));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    assertEquals("ADMIN_NOT_CONFIGURED", exception.getCode());
  }

  @Test
  void rejectsWrongKeyAndAcceptsConfiguredKey() {
    AdminKeyVerifier verifier = new AdminKeyVerifier("0123456789abcdef0123456789abcdef");
    assertThrows(
        ApiException.class, () -> verifier.requireValid("fedcba9876543210fedcba9876543210"));
    assertDoesNotThrow(() -> verifier.requireValid("0123456789abcdef0123456789abcdef"));
  }
}
