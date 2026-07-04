package com.example.demo.auth;

import com.example.demo.audit.AuditEvent;
import com.example.demo.audit.AuditService;
import com.example.demo.auth.dto.SessionResponse;
import com.example.demo.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessionManagementService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final AuditService auditService;

	public SessionManagementService(RefreshTokenRepository refreshTokenRepository, AuditService auditService) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<SessionResponse> activeSessions(UserAccount user, Optional<String> currentRefreshToken) {
		String currentHash = currentRefreshToken.map(this::hash).orElse(null);
		return refreshTokenRepository.findByUserOrderByCreatedAtDesc(user).stream()
				.filter(RefreshToken::isActive)
				.map(token -> SessionResponse.from(token, token.getTokenHash().equals(currentHash)))
				.toList();
	}

	@Transactional
	public boolean revokeSession(UserAccount user, UUID sessionId, Optional<String> currentRefreshToken, RequestMetadata metadata) {
		RefreshToken token = refreshTokenRepository.findByIdAndUser(sessionId, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

		boolean current = currentRefreshToken.map(this::hash)
				.map(hash -> hash.equals(token.getTokenHash()))
				.orElse(false);

		if (token.isActive()) {
			token.revoke();
			audit(user, "session_revoked", metadata, Map.of(
					"sessionId", token.getId().toString(),
					"current", Boolean.toString(current)
			));
		}
		return current;
	}

	@Transactional
	public int revokeOtherSessions(UserAccount user, Optional<String> currentRefreshToken, RequestMetadata metadata) {
		String currentHash = currentRefreshToken.map(this::hash).orElse(null);
		List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevokedFalse(user).stream()
				.filter(RefreshToken::isActive)
				.filter(token -> currentHash == null || !token.getTokenHash().equals(currentHash))
				.toList();

		activeTokens.forEach(RefreshToken::revoke);
		audit(user, "other_sessions_revoked", metadata, Map.of("count", Integer.toString(activeTokens.size())));
		return activeTokens.size();
	}

	private void audit(UserAccount user, String action, RequestMetadata metadata, Map<String, String> extraMetadata) {
		java.util.HashMap<String, String> values = new java.util.HashMap<>(extraMetadata);
		values.put("ipAddress", metadata.ipAddress());
		values.put("userAgent", metadata.userAgent());
		auditService.publish(new AuditEvent(
				user.getEmail(),
				action,
				"auth.session",
				metadata.requestId(),
				Instant.now(),
				Map.copyOf(values)
		));
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to hash refresh token", exception);
		}
	}
}
