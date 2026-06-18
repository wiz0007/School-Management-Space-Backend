package com.example.demo.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
		int rateLimitCapacity,
		Duration rateLimitWindow
) {
	public SecurityProperties {
		rateLimitCapacity = rateLimitCapacity <= 0 ? 120 : rateLimitCapacity;
		rateLimitWindow = rateLimitWindow == null ? Duration.ofMinutes(1) : rateLimitWindow;
	}
}
