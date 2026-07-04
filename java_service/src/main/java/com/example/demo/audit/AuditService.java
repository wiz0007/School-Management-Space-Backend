package com.example.demo.audit;

import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
	private static final Logger log = LoggerFactory.getLogger(AuditService.class);
	private final AuditLogRepository auditLogRepository;

	public AuditService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	public void publish(AuditEvent event) {
		AuditLog auditLog = new AuditLog();
		auditLog.setActor(limit(event.actor(), 180));
		auditLog.setAction(limit(event.action(), 120));
		auditLog.setResource(limit(event.resource(), 160));
		auditLog.setRequestId(limit(event.requestId(), 80));
		auditLog.setOccurredAt(event.occurredAt());
		auditLog.setMetadataJson(toJson(event.metadata()));
		auditLogRepository.save(auditLog);

		log.info(
				"audit actor={} action={} resource={} requestId={}",
				event.actor(),
				event.action(),
				event.resource(),
				event.requestId()
		);
	}

	private String toJson(Map<String, String> metadata) {
		if (metadata == null || metadata.isEmpty()) {
			return "{}";
		}
		return metadata.entrySet().stream()
				.map(entry -> quote(entry.getKey()) + ":" + quote(entry.getValue()))
				.collect(Collectors.joining(",", "{", "}"));
	}

	private String quote(String value) {
		String safeValue = value == null ? "" : value;
		return "\"" + safeValue
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "\\r")
				.replace("\n", "\\n")
				.replace("\t", "\\t") + "\"";
	}

	private String limit(String value, int maxLength) {
		String safeValue = value == null || value.isBlank() ? "unknown" : value;
		return safeValue.length() <= maxLength ? safeValue : safeValue.substring(0, maxLength);
	}
}
