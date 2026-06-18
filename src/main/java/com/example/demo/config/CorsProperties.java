package com.example.demo.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
		List<String> allowedOrigins,
		List<String> allowedMethods,
		List<String> allowedHeaders
) {
	public CorsProperties {
		allowedOrigins = allowedOrigins == null ? List.of("http://localhost:4200") : allowedOrigins;
		allowedMethods = allowedMethods == null ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") : allowedMethods;
		allowedHeaders = allowedHeaders == null ? List.of("Authorization", "Content-Type", "X-Request-Id") : allowedHeaders;
	}
}
