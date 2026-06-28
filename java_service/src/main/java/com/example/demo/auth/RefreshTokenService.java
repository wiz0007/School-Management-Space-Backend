package com.example.demo.auth;

import com.example.demo.config.SecurityProperties;
import com.example.demo.security.JwtService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
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

	public RefreshTokenService(
			RefreshTokenRepository refreshTokenRepository,
			UserRepository userRepository,
			JwtService jwtService,
			SecurityProperties properties
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.properties = properties;
	}

	@Transactional
	public String issue(UserAccount user) {
		String refreshToken = jwtService.createRefreshToken(user);
		RefreshToken storedToken = new RefreshToken();
		storedToken.setUser(user);
		storedToken.setTokenHash(hash(refreshToken));
		storedToken.setExpiresAt(Instant.now().plus(properties.jwtRefreshExpiration()));
		refreshTokenRepository.save(storedToken);
		return refreshToken;
	}

	@Transactional
	public RefreshRotation rotate(String refreshToken) {
		JwtService.TokenClaims claims = jwtService.validateRefreshToken(refreshToken)
				.orElseThrow(() -> invalidRefreshToken());

		UserAccount user = userRepository.findByEmail(claims.subject())
				.filter(UserAccount::isEnabled)
				.orElseThrow(() -> invalidRefreshToken());

		RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash(refreshToken))
				.orElseThrow(() -> stolenRefreshToken(user));

		if (!storedToken.isActive() || !storedToken.getUser().getId().equals(user.getId())) {
			revokeAllForUser(user);
			throw invalidRefreshToken();
		}

		storedToken.revoke();
		String newAccessToken = jwtService.createAccessToken(user);
		String newRefreshToken = issue(user);
		return new RefreshRotation(user, newAccessToken, newRefreshToken);
	}

	@Transactional
	public void revoke(String refreshToken) {
		jwtService.validateRefreshToken(refreshToken)
				.flatMap(claims -> userRepository.findByEmail(claims.subject()))
				.ifPresent(this::revokeAllForUser);
	}

	@Transactional
	public void revokeAllForUser(UserAccount user) {
		refreshTokenRepository.findByUserAndRevokedFalse(user).stream()
				.filter(RefreshToken::isActive)
				.forEach(RefreshToken::revoke);
	}

	private ResponseStatusException invalidRefreshToken() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired. Please sign in again.");
	}

	private ResponseStatusException stolenRefreshToken(UserAccount user) {
		revokeAllForUser(user);
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session could not be verified. Please sign in again.");
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