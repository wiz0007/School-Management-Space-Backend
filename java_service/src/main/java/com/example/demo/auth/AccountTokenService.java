package com.example.demo.auth;

import com.example.demo.audit.AuditEvent;
import com.example.demo.audit.AuditService;
import com.example.demo.config.SecurityProperties;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountTokenService {
	private final AccountTokenRepository accountTokenRepository;
	private final UserRepository userRepository;
	private final SecurityProperties securityProperties;
	private final AuditService auditService;
	private final AccountTokenDeliveryService accountTokenDeliveryService;
	private final SecureRandom secureRandom = new SecureRandom();

	public AccountTokenService(
			AccountTokenRepository accountTokenRepository,
			UserRepository userRepository,
			SecurityProperties securityProperties,
			AuditService auditService,
			AccountTokenDeliveryService accountTokenDeliveryService
	) {
		this.accountTokenRepository = accountTokenRepository;
		this.userRepository = userRepository;
		this.securityProperties = securityProperties;
		this.auditService = auditService;
		this.accountTokenDeliveryService = accountTokenDeliveryService;
	}

	@Transactional
	public void issueEmailVerification(UserAccount user, RequestMetadata metadata) {
		issue(user, AccountTokenPurpose.EMAIL_VERIFICATION, securityProperties.emailVerificationExpiration(), metadata);
	}

	@Transactional
	public void issuePasswordReset(UserAccount user, RequestMetadata metadata) {
		issue(user, AccountTokenPurpose.PASSWORD_RESET, securityProperties.passwordResetExpiration(), metadata);
	}

	@Transactional
	public UserAccount consumeEmailVerification(String rawToken, RequestMetadata metadata) {
		AccountToken token = consume(rawToken, AccountTokenPurpose.EMAIL_VERIFICATION, metadata);
		UserAccount user = token.getUser();
		user.markEmailVerified();
		UserAccount savedUser = userRepository.save(user);
		audit(savedUser, "email_verified", metadata, Map.of());
		return savedUser;
	}

	@Transactional
	public UserAccount consumePasswordReset(String rawToken, RequestMetadata metadata) {
		AccountToken token = consume(rawToken, AccountTokenPurpose.PASSWORD_RESET, metadata);
		UserAccount user = token.getUser();
		audit(user, "password_reset_token_consumed", metadata, Map.of());
		return user;
	}

	private void issue(UserAccount user, AccountTokenPurpose purpose, Duration expiresIn, RequestMetadata metadata) {
		accountTokenRepository.findByUserAndPurposeAndConsumedAtIsNull(user, purpose)
				.forEach(AccountToken::consume);

		String rawToken = randomToken();
		AccountToken token = new AccountToken();
		token.setUser(user);
		token.setPurpose(purpose);
		token.setTokenHash(hash(rawToken));
		token.setExpiresAt(Instant.now().plus(expiresIn));
		token.setIpAddress(limit(metadata.ipAddress(), 80));
		token.setUserAgent(limit(metadata.userAgent(), 300));
		accountTokenRepository.save(token);
		accountTokenDeliveryService.deliver(user, purpose, rawToken);

		audit(user, purpose.name().toLowerCase() + "_issued", metadata, Map.of(
				"expiresAt", token.getExpiresAt().toString(),
				"delivery", securityProperties.accountTokenDevDeliveryEnabled() ? "development_log" : "pending_email_adapter"
		));
	}

	private AccountToken consume(String rawToken, AccountTokenPurpose purpose, RequestMetadata metadata) {
		AccountToken token = accountTokenRepository.findByTokenHashAndPurpose(hash(rawToken), purpose)
				.orElseThrow(() -> invalidToken(purpose));

		if (!token.isActive()) {
			audit(token.getUser(), purpose.name().toLowerCase() + "_invalid_or_expired", metadata, Map.of());
			throw invalidToken(purpose);
		}

		token.consume();
		return accountTokenRepository.save(token);
	}

	private ResponseStatusException invalidToken(AccountTokenPurpose purpose) {
		String message = purpose == AccountTokenPurpose.EMAIL_VERIFICATION
				? "Verification link is invalid or expired"
				: "Password reset link is invalid or expired";
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	private String randomToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to hash account token", exception);
		}
	}

	private void audit(UserAccount user, String action, RequestMetadata metadata, Map<String, String> extraMetadata) {
		java.util.HashMap<String, String> values = new java.util.HashMap<>(extraMetadata);
		values.put("ipAddress", metadata.ipAddress());
		values.put("userAgent", metadata.userAgent());
		auditService.publish(new AuditEvent(
				user.getEmail(),
				action,
				"auth.account_token",
				metadata.requestId(),
				Instant.now(),
				Map.copyOf(values)
		));
	}

	private String limit(String value, int maxLength) {
		String safeValue = value == null || value.isBlank() ? "unknown" : value;
		return safeValue.length() <= maxLength ? safeValue : safeValue.substring(0, maxLength);
	}
}
