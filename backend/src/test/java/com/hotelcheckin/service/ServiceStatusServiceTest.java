package com.hotelcheckin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class ServiceStatusServiceTest {
  @Test
  void reportsDatabaseFailureAsDegradedWithoutThrowing() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    when(jdbc.queryForObject("SELECT 1", Integer.class))
        .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("unavailable"));
    var response = new ServiceStatusService(jdbc, new MaintenanceState(false)).getStatus();
    verify(jdbc).setQueryTimeout(2);
    assertEquals("DEGRADED", response.status());
    assertEquals("DOWN", response.components().database());
    assertNotNull(response.checkedAt());
  }

  @Test
  void runtimeMaintenanceCanBeEnabledAndClearedWithoutDatabase() {
    MaintenanceState state = new MaintenanceState(false);
    state.update(true, "Planned work", null);
    assertTrue(state.get().enabled());
    state.update(false, null, null);
    assertFalse(state.get().enabled());
  }
}
