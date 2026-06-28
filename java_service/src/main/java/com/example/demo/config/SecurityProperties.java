package com.example.demo.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
		int rateLimitCapacity,
		Duration rateLimitWindow,
		String jwtSecret,
		Duration jwtExpiration,
		Duration jwtRefreshExpiration,
		String authCookieName,
		String refreshCookieName,
		boolean authCookieSecure
) {
	public SecurityProperties {
		rateLimitCapacity = rateLimitCapacity <= 0 ? 120 : rateLimitCapacity;
		rateLimitWindow = rateLimitWindow == null ? Duration.ofMinutes(1) : rateLimitWindow;
		jwtSecret = jwtSecret == null || jwtSecret.isBlank()
				? "dev-only-change-this-secret-before-production-123456"
				: jwtSecret;
		jwtExpiration = jwtExpiration == null ? Duration.ofMinutes(15) : jwtExpiration;
		jwtRefreshExpiration = jwtRefreshExpiration == null ? Duration.ofDays(7) : jwtRefreshExpiration;
		authCookieName = authCookieName == null || authCookieName.isBlank()
				? "schoolsys_session"
				: authCookieName;
		refreshCookieName = refreshCookieName == null || refreshCookieName.isBlank()
				? "schoolsys_refresh"
				: refreshCookieName;
	}
}