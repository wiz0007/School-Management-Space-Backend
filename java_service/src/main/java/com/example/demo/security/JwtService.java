package com.example.demo.security;

import com.example.demo.config.SecurityProperties;
import com.example.demo.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private static final Pattern SUBJECT_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");
	private static final Pattern EXPIRY_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
	private static final Pattern TYPE_PATTERN = Pattern.compile("\"typ\"\\s*:\\s*\"([^\"]+)\"");
	private static final Pattern JTI_PATTERN = Pattern.compile("\"jti\"\\s*:\\s*\"([^\"]+)\"");
	private static final String ACCESS_TYPE = "access";
	private static final String REFRESH_TYPE = "refresh";
	private final SecurityProperties properties;

	public JwtService(SecurityProperties properties) {
		this.properties = properties;
	}

	public String createAccessToken(UserAccount user) {
		return createToken(user, ACCESS_TYPE, properties.jwtExpiration());
	}

	public String createRefreshToken(UserAccount user) {
		return createToken(user, REFRESH_TYPE, properties.jwtRefreshExpiration());
	}

	public Optional<TokenClaims> validateAccessToken(String token) {
		return validateToken(token, ACCESS_TYPE);
	}

	public Optional<TokenClaims> validateRefreshToken(String token) {
		return validateToken(token, REFRESH_TYPE);
	}

	public Optional<String> subject(String token) {
		return validateAccessToken(token).map(TokenClaims::subject);
	}

	public long expiresInSeconds() {
		return properties.jwtExpiration().toSeconds();
	}

	public long refreshExpiresInSeconds() {
		return properties.jwtRefreshExpiration().toSeconds();
	}

	private String createToken(UserAccount user, String type, Duration duration) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(duration);
		String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payload = "{"
				+ "\"sub\":\"" + escape(user.getEmail()) + "\","
				+ "\"uid\":\"" + user.getId() + "\","
				+ "\"role\":\"" + user.getRole().name() + "\","
				+ "\"typ\":\"" + type + "\","
				+ "\"jti\":\"" + UUID.randomUUID() + "\","
				+ "\"iat\":" + now.getEpochSecond() + ","
				+ "\"exp\":" + expiresAt.getEpochSecond()
				+ "}";
		String unsignedToken = base64Url(header.getBytes(StandardCharsets.UTF_8))
				+ "."
				+ base64Url(payload.getBytes(StandardCharsets.UTF_8));
		return unsignedToken + "." + sign(unsignedToken);
	}

	private Optional<TokenClaims> validateToken(String token, String expectedType) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}

		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			return Optional.empty();
		}

		try {
			String unsignedToken = parts[0] + "." + parts[1];
			if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
				return Optional.empty();
			}

			String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
			Long expiresAt = longClaim(payload, EXPIRY_PATTERN);
			Optional<String> subject = stringClaim(payload, SUBJECT_PATTERN);
			Optional<String> type = stringClaim(payload, TYPE_PATTERN);
			Optional<String> jwtId = stringClaim(payload, JTI_PATTERN);
			if (expiresAt == null || Instant.now().getEpochSecond() >= expiresAt) {
				return Optional.empty();
			}
			if (subject.isEmpty() || type.isEmpty() || jwtId.isEmpty() || !expectedType.equals(type.get())) {
				return Optional.empty();
			}

			return Optional.of(new TokenClaims(subject.get(), type.get(), jwtId.get(), Instant.ofEpochSecond(expiresAt)));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private Optional<String> stringClaim(String payload, Pattern pattern) {
		Matcher matcher = pattern.matcher(payload);
		return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
	}

	private Long longClaim(String payload, Pattern pattern) {
		Matcher matcher = pattern.matcher(payload);
		return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(properties.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to sign token", exception);
		}
	}

	private boolean constantTimeEquals(String left, String right) {
		return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
	}

	private String base64Url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	public record TokenClaims(String subject, String type, String jwtId, Instant expiresAt) {
	}
}