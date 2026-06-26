package com.example.demo.security;

import com.example.demo.config.SecurityProperties;
import com.example.demo.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private static final Pattern SUBJECT_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");
	private static final Pattern EXPIRY_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
	private final SecurityProperties properties;

	public JwtService(SecurityProperties properties) {
		this.properties = properties;
	}

	public String createAccessToken(UserAccount user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.jwtExpiration());
		String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payload = "{"
				+ "\"sub\":\"" + escape(user.getEmail()) + "\","
				+ "\"uid\":\"" + user.getId() + "\","
				+ "\"role\":\"" + user.getRole().name() + "\","
				+ "\"iat\":" + now.getEpochSecond() + ","
				+ "\"exp\":" + expiresAt.getEpochSecond()
				+ "}";
		String unsignedToken = base64Url(header.getBytes(StandardCharsets.UTF_8))
				+ "."
				+ base64Url(payload.getBytes(StandardCharsets.UTF_8));
		return unsignedToken + "." + sign(unsignedToken);
	}

	public Optional<String> subject(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}

		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			return Optional.empty();
		}

		String unsignedToken = parts[0] + "." + parts[1];
		if (!sign(unsignedToken).equals(parts[2])) {
			return Optional.empty();
		}

		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		Long expiresAt = longClaim(payload, EXPIRY_PATTERN);
		if (expiresAt == null || Instant.now().getEpochSecond() >= expiresAt) {
			return Optional.empty();
		}

		return stringClaim(payload, SUBJECT_PATTERN);
	}

	public long expiresInSeconds() {
		return properties.jwtExpiration().toSeconds();
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
			throw new IllegalStateException("Unable to sign access token", exception);
		}
	}

	private String base64Url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
