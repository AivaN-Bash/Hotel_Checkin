package com.hotelcheckin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelcheckin.api.ErrorResponse;
import com.hotelcheckin.service.MaintenanceState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MaintenanceFilter extends OncePerRequestFilter {
  private final MaintenanceState state;
  private final ObjectMapper mapper;

  public MaintenanceFilter(MaintenanceState state, ObjectMapper mapper) {
    this.state = state;
    this.mapper = mapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    boolean apiPath = path.startsWith("/api/v1/");
    boolean exempt =
        path.equals("/api/v1/status")
            || path.equals("/api/v1/health")
            || path.equals("/api/v1/admin/maintenance")
            || request.getMethod().equals("OPTIONS");
    var current = state.get();
    if (apiPath && !exempt && current.enabled()) {
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      mapper.writeValue(
          response.getWriter(),
          new ErrorResponse(
              OffsetDateTime.now(ZoneOffset.UTC),
              HttpStatus.SERVICE_UNAVAILABLE.value(),
              "MAINTENANCE",
              current.message()));
      return;
    }
    chain.doFilter(request, response);
  }
}
