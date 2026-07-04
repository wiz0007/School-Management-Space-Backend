package com.example.demo.auth.dto;

import com.example.demo.auth.RefreshToken;
import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
		UUID id,
		String tokenFamilyId,
		String ipAddress,
		String userAgent,
		Instant createdAt,
		Instant lastUsedAt,
		Instant expiresAt,
		boolean current
) {
	public static SessionResponse from(RefreshToken token, boolean current) {
		return new SessionResponse(
				token.getId(),
				token.getTokenFamilyId(),
				token.getIpAddress(),
				token.getUserAgent(),
				token.getCreatedAt(),
				token.getLastUsedAt(),
				token.getExpiresAt(),
				current
		);
	}
}
