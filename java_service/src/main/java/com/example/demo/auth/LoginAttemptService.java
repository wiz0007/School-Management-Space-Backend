package com.example.demo.auth;

import com.example.demo.config.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginAttemptService {
	private final LoginAttemptRepository loginAttemptRepository;
	private final SecurityProperties securityProperties;

	public LoginAttemptService(LoginAttemptRepository loginAttemptRepository, SecurityProperties securityProperties) {
		this.loginAttemptRepository = loginAttemptRepository;
		this.securityProperties = securityProperties;
	}

	public void assertLoginAllowed(String email, RequestMetadata metadata) {
		Instant since = Instant.now().minus(securityProperties.loginFailureWindow());
		String emailHash = hashEmail(email);
		long emailFailures = loginAttemptRepository
				.countByEmailHashAndSuccessfulFalseAndAttemptedAtAfter(emailHash, since);
		long ipFailures = loginAttemptRepository
				.countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(metadata.ipAddress(), since);

		if (emailFailures >= securityProperties.loginEmailFailureLimit()
				|| ipFailures >= securityProperties.loginIpFailureLimit()) {
			throw new ResponseStatusException(
					HttpStatus.TOO_MANY_REQUESTS,
					"Too many sign-in attempts. Please wait before trying again."
			);
		}
	}

	@Transactional
	public void recordFailure(String email, RequestMetadata metadata, String reason) {
		record(email, metadata, false, reason);
	}

	@Transactional
	public void recordSuccess(String email, RequestMetadata metadata) {
		record(email, metadata, true, "success");
	}

	private void record(String email, RequestMetadata metadata, boolean successful, String reason) {
		LoginAttempt attempt = new LoginAttempt();
		attempt.setEmailHash(hashEmail(email));
		attempt.setIpAddress(limit(metadata.ipAddress(), 80));
		attempt.setUserAgent(limit(metadata.userAgent(), 300));
		attempt.setSuccessful(successful);
		attempt.setReason(limit(reason, 80));
		attempt.setRequestId(limit(metadata.requestId(), 80));
		attempt.setAttemptedAt(Instant.now());
		loginAttemptRepository.save(attempt);
	}

	private String hashEmail(String email) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	private String limit(String value, int maxLength) {
		String safeValue = value == null || value.isBlank() ? "unknown" : value;
		return safeValue.length() <= maxLength ? safeValue : safeValue.substring(0, maxLength);
	}
}
