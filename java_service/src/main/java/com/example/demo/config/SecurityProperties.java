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
		boolean authCookieSecure,
		Duration loginFailureWindow,
		int loginEmailFailureLimit,
		int loginIpFailureLimit,
		Duration emailVerificationExpiration,
		Duration passwordResetExpiration,
		String publicFrontendUrl,
		boolean accountTokenDevDeliveryEnabled
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
		loginFailureWindow = loginFailureWindow == null ? Duration.ofMinutes(15) : loginFailureWindow;
		loginEmailFailureLimit = loginEmailFailureLimit <= 0 ? 5 : loginEmailFailureLimit;
		loginIpFailureLimit = loginIpFailureLimit <= 0 ? 20 : loginIpFailureLimit;
		emailVerificationExpiration = emailVerificationExpiration == null ? Duration.ofHours(24) : emailVerificationExpiration;
		passwordResetExpiration = passwordResetExpiration == null ? Duration.ofMinutes(30) : passwordResetExpiration;
		publicFrontendUrl = publicFrontendUrl == null || publicFrontendUrl.isBlank()
				? "http://localhost:4200"
				: publicFrontendUrl;
	}
}
