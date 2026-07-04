package com.example.demo.auth;

import com.example.demo.audit.AuditEvent;
import com.example.demo.audit.AuditService;
import com.example.demo.config.SecurityProperties;
import com.example.demo.security.JwtService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final SecurityProperties properties;
	private final AuditService auditService;

	public RefreshTokenService(
			RefreshTokenRepository refreshTokenRepository,
			UserRepository userRepository,
			JwtService jwtService,
			SecurityProperties properties,
			AuditService auditService
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.properties = properties;
		this.auditService = auditService;
	}

	@Transactional
	public String issue(UserAccount user, RequestMetadata metadata) {
		return issue(user, metadata, UUID.randomUUID().toString());
	}

	@Transactional
	public String issue(UserAccount user, RequestMetadata metadata, String tokenFamilyId) {
		String refreshToken = jwtService.createRefreshToken(user);
		JwtService.TokenClaims claims = jwtService.validateRefreshToken(refreshToken)
				.orElseThrow(() -> new IllegalStateException("Issued refresh token could not be validated"));

		RefreshToken storedToken = new RefreshToken();
		storedToken.setUser(user);
		storedToken.setTokenHash(hash(refreshToken));
		storedToken.setTokenId(claims.jwtId());
		storedToken.setTokenFamilyId(tokenFamilyId);
		storedToken.setExpiresAt(Instant.now().plus(properties.jwtRefreshExpiration()));
		storedToken.setIpAddress(metadata.ipAddress());
		storedToken.setUserAgent(metadata.userAgent());
		refreshTokenRepository.save(storedToken);
		return refreshToken;
	}

	@Transactional
	public RefreshRotation rotate(String refreshToken, RequestMetadata metadata) {
		JwtService.TokenClaims claims = jwtService.validateRefreshToken(refreshToken)
				.orElseThrow(() -> invalidRefreshToken());

		UserAccount user = userRepository.findByEmail(claims.subject())
				.filter(UserAccount::isEnabled)
				.orElseThrow(() -> invalidRefreshToken());

		String currentHash = hash(refreshToken);
		RefreshToken storedToken = refreshTokenRepository.findByTokenHash(currentHash)
				.orElseThrow(() -> stolenRefreshToken(user, metadata));

		if (!storedToken.getUser().getId().equals(user.getId())) {
			revokeAllForUser(user);
			audit(user, "refresh_user_mismatch", metadata, Map.of("tokenFamilyId", storedToken.getTokenFamilyId()));
			throw invalidRefreshToken();
		}

		if (!storedToken.isActive()) {
			storedToken.markReuseDetected();
			revokeFamily(user, storedToken.getTokenFamilyId());
			audit(user, "refresh_reuse_detected", metadata, Map.of("tokenFamilyId", storedToken.getTokenFamilyId()));
			throw invalidRefreshToken();
		}

		storedToken.markUsed();
		String newAccessToken = jwtService.createAccessToken(user);
		String newRefreshToken = issue(user, metadata, storedToken.getTokenFamilyId());
		storedToken.revoke(hash(newRefreshToken));
		audit(user, "refresh_rotated", metadata, Map.of("tokenFamilyId", storedToken.getTokenFamilyId()));
		return new RefreshRotation(user, newAccessToken, newRefreshToken);
	}

	@Transactional
	public void revoke(String refreshToken, RequestMetadata metadata) {
		jwtService.validateRefreshToken(refreshToken)
				.flatMap(claims -> userRepository.findByEmail(claims.subject()))
				.ifPresent(user -> {
					revokeAllForUser(user);
					audit(user, "session_logout", metadata, Map.of());
				});
	}

	@Transactional
	public void revokeAllForUser(UserAccount user) {
		refreshTokenRepository.findByUserAndRevokedFalse(user).stream()
				.filter(RefreshToken::isActive)
				.forEach(RefreshToken::revoke);
	}

	@Transactional
	public void revokeFamily(UserAccount user, String tokenFamilyId) {
		refreshTokenRepository.findByUserAndTokenFamilyId(user, tokenFamilyId).stream()
				.filter(RefreshToken::isActive)
				.forEach(RefreshToken::revoke);
	}

	private ResponseStatusException invalidRefreshToken() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired. Please sign in again.");
	}

	private ResponseStatusException stolenRefreshToken(UserAccount user, RequestMetadata metadata) {
		revokeAllForUser(user);
		audit(user, "refresh_missing_database_record", metadata, Map.of("risk", "possible_stolen_token"));
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session could not be verified. Please sign in again.");
	}

	private void audit(UserAccount user, String action, RequestMetadata metadata, Map<String, String> extraMetadata) {
		auditService.publish(new AuditEvent(
				user.getEmail(),
				action,
				"auth.refresh_token",
				metadata.requestId(),
				Instant.now(),
				mergeMetadata(metadata, extraMetadata)
		));
	}

	private Map<String, String> mergeMetadata(RequestMetadata metadata, Map<String, String> extraMetadata) {
		java.util.HashMap<String, String> values = new java.util.HashMap<>(extraMetadata);
		values.put("ipAddress", metadata.ipAddress());
		values.put("userAgent", metadata.userAgent());
		return Map.copyOf(values);
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

	public record RefreshRotation(UserAccount user, String accessToken, String refreshToken) {
	}
}