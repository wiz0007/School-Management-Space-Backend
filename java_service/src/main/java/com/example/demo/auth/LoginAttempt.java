package com.example.demo.auth;

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
@Table(name = "login_attempts", indexes = {
		@Index(name = "idx_login_attempts_email_hash", columnList = "emailHash"),
		@Index(name = "idx_login_attempts_ip_address", columnList = "ipAddress"),
		@Index(name = "idx_login_attempts_attempted_at", columnList = "attemptedAt"),
		@Index(name = "idx_login_attempts_email_time", columnList = "emailHash,attemptedAt"),
		@Index(name = "idx_login_attempts_ip_time", columnList = "ipAddress,attemptedAt")
})
public class LoginAttempt {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 96)
	private String emailHash;

	@Column(nullable = false, length = 80)
	private String ipAddress;

	@Column(nullable = false, length = 300)
	private String userAgent;

	@Column(nullable = false)
	private boolean successful;

	@Column(nullable = false, length = 80)
	private String reason;

	@Column(nullable = false, length = 80)
	private String requestId;

	@Column(nullable = false)
	private Instant attemptedAt;

	@PrePersist
	void prePersist() {
		if (attemptedAt == null) {
			attemptedAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getEmailHash() {
		return emailHash;
	}

	public void setEmailHash(String emailHash) {
		this.emailHash = emailHash;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Instant getAttemptedAt() {
		return attemptedAt;
	}

	public void setAttemptedAt(Instant attemptedAt) {
		this.attemptedAt = attemptedAt;
	}
}
