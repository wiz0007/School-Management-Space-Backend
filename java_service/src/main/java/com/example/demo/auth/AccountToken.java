package com.example.demo.auth;

import com.example.demo.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_tokens", indexes = {
		@Index(name = "idx_account_tokens_hash", columnList = "tokenHash", unique = true),
		@Index(name = "idx_account_tokens_user_purpose", columnList = "user_id,purpose"),
		@Index(name = "idx_account_tokens_expires_at", columnList = "expiresAt")
})
public class AccountToken {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AccountTokenPurpose purpose;

	@Column(nullable = false, unique = true, length = 96)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant consumedAt;

	@Column(nullable = false, length = 80)
	private String ipAddress;

	@Column(nullable = false, length = 300)
	private String userAgent;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public boolean isActive() {
		return consumedAt == null && expiresAt.isAfter(Instant.now());
	}

	public void consume() {
		consumedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UserAccount getUser() {
		return user;
	}

	public void setUser(UserAccount user) {
		this.user = user;
	}

	public AccountTokenPurpose getPurpose() {
		return purpose;
	}

	public void setPurpose(AccountTokenPurpose purpose) {
		this.purpose = purpose;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getConsumedAt() {
		return consumedAt;
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
}
