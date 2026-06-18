package com.example.demo.health;

import java.time.Instant;

public record HealthStatus(
		String service,
		String status,
		Instant checkedAt
) {
}
