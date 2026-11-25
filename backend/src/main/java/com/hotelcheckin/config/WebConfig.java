package com.hotelcheckin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private static final String PRODUCTION_FRONTEND_ORIGIN =
      "https://frontend-nine-kohl-nzqk24mi4s.vercel.app";

  private final String[] allowedOrigins;

  public WebConfig(@Value("${app.cors-origins}") String configuredOrigins) {
    this.allowedOrigins =
        java.util.stream.Stream.concat(
                java.util.Arrays.stream(configuredOrigins.split(",")),
                java.util.stream.Stream.of(PRODUCTION_FRONTEND_ORIGIN))
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
