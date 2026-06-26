package com.example.demo.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
		int rateLimitCapacity,
		Duration rateLimitWindow,
		String jwtSecret,
		Duration jwtExpiration
) {
	public SecurityProperties {
		rateLimitCapacity = rateLimitCapacity <= 0 ? 120 : rateLimitCapacity;
		rateLimitWindow = rateLimitWindow == null ? Duration.ofMinutes(1) : rateLimitWindow;
		jwtSecret = jwtSecret == null || jwtSecret.isBlank()
				? "dev-only-change-this-secret-before-production-123456"
				: jwtSecret;
		jwtExpiration = jwtExpiration == null ? Duration.ofHours(8) : jwtExpiration;
	}
}
