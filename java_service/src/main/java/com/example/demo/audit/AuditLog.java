package com.example.demo.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
		@Index(name = "idx_audit_logs_actor", columnList = "actor"),
		@Index(name = "idx_audit_logs_action", columnList = "action"),
		@Index(name = "idx_audit_logs_occurred_at", columnList = "occurredAt"),
		@Index(name = "idx_audit_logs_request_id", columnList = "requestId")
})
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 180)
	private String actor;

	@Column(nullable = false, length = 120)
	private String action;

	@Column(nullable = false, length = 160)
	private String resource;

	@Column(nullable = false, length = 80)
	private String requestId;

	@Column(nullable = false)
	private Instant occurredAt;

	@Column(columnDefinition = "text")
	private String metadataJson;

	@PrePersist
	void prePersist() {
		if (occurredAt == null) {
			occurredAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(Instant occurredAt) {
		this.occurredAt = occurredAt;
	}

	public String getMetadataJson() {
		return metadataJson;
	}

	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
