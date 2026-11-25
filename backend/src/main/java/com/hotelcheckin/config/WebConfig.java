package com.hotelcheckin.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final String[] allowedOrigins;

  public WebConfig(@Value("${app.cors-origins}") String configuredOrigins) {
    this.allowedOrigins =
        Arrays.stream(configuredOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .distinct()
            .toArray(String[]::new);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("Content-Type", "Idempotency-Key", "X-Admin-Key")
        .maxAge(3600);
  }
}
