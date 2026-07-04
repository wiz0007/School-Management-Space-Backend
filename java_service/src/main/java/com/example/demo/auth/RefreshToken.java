package com.example.demo.auth;

import com.example.demo.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(
		name = "refresh_tokens",
		indexes = {
				@Index(name = "idx_refresh_token_hash", columnList = "tokenHash", unique = true),
				@Index(name = "idx_refresh_token_user", columnList = "user_id"),
				@Index(name = "idx_refresh_token_family", columnList = "tokenFamilyId"),
				@Index(name = "idx_refresh_token_jti", columnList = "tokenId", unique = true)
		}
)
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(nullable = false, unique = true, length = 128)
	private String tokenHash;

	@Column(nullable = false, unique = true, length = 80)
	private String tokenId;

	@Column(nullable = false, length = 80)
	private String tokenFamilyId;

	@Column(length = 128)
	private String replacedByTokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean revoked = false;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant lastUsedAt;
	private Instant revokedAt;
	private Instant reuseDetectedAt;

	@Column(length = 64)
	private String ipAddress;

	@Column(length = 512)
	private String userAgent;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
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

	public String getTokenHash() {
		return tokenHash;
	}

	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getTokenFamilyId() {
		return tokenFamilyId;
	}

	public void setTokenFamilyId(String tokenFamilyId) {
		this.tokenFamilyId = tokenFamilyId;
	}

	public String getReplacedByTokenHash() {
		return replacedByTokenHash;
	}

	public void setReplacedByTokenHash(String replacedByTokenHash) {
		this.replacedByTokenHash = replacedByTokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void revoke() {
		revoked = true;
		revokedAt = Instant.now();
	}

	public void revoke(String replacementHash) {
		replacedByTokenHash = replacementHash;
		revoke();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastUsedAt() {
		return lastUsedAt;
	}

	public void markUsed() {
		lastUsedAt = Instant.now();
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public Instant getReuseDetectedAt() {
		return reuseDetectedAt;
	}

	public void markReuseDetected() {
		reuseDetectedAt = Instant.now();
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

	public boolean isActive() {
		return !revoked && Instant.now().isBefore(expiresAt);
	}
}