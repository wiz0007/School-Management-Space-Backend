package com.example.demo.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
		String actor,
		String action,
		String resource,
		String requestId,
		Instant occurredAt,
		Map<String, String> metadata
) {
	public static AuditEvent system(String action, String resource, String requestId) {
		return new AuditEvent("system", action, resource, requestId, Instant.now(), Map.of());
	}
}
