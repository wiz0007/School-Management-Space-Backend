package com.example.demo.auth;

import com.example.demo.audit.AuditEvent;
import com.example.demo.audit.AuditService;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.AuthSession;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.security.JwtService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final AuditService auditService;
	private final LoginAttemptService loginAttemptService;
	private final AccountTokenService accountTokenService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			AuditService auditService,
			LoginAttemptService loginAttemptService,
			AccountTokenService accountTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.auditService = auditService;
		this.loginAttemptService = loginAttemptService;
		this.accountTokenService = accountTokenService;
	}

	@Transactional
	public AuthSession register(RegisterRequest request, RequestMetadata metadata) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			audit(email, "register_conflict", metadata, Map.of("reason", "email_exists"));
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		UserAccount user = new UserAccount();
		user.setFullName(request.fullName().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));

		UserAccount savedUser = userRepository.save(user);
		accountTokenService.issueEmailVerification(savedUser, metadata);
		audit(savedUser.getEmail(), "register_success", metadata, Map.of("userId", savedUser.getId().toString()));
		return sessionFor(savedUser, metadata);
	}

	@Transactional
	public AuthSession login(LoginRequest request, RequestMetadata metadata) {
		String email = normalizeEmail(request.email());
		loginAttemptService.assertLoginAllowed(email, metadata);

		UserAccount user = userRepository.findByEmail(email)
				.filter(UserAccount::isEnabled)
				.orElse(null);

		if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			loginAttemptService.recordFailure(email, metadata, "invalid_credentials");
			audit(email, "login_failed", metadata, Map.of("reason", "invalid_credentials"));
			throw invalidCredentials();
		}

		loginAttemptService.recordSuccess(user.getEmail(), metadata);
		audit(user.getEmail(), "login_success", metadata, Map.of(
				"userId", user.getId().toString(),
				"emailVerified", Boolean.toString(user.isEmailVerified())
		));
		return sessionFor(user, metadata);
	}

	@Transactional
	public UserProfileResponse verifyEmail(String token, RequestMetadata metadata) {
		UserAccount user = accountTokenService.consumeEmailVerification(token, metadata);
		return responseFor(user).user();
	}

	@Transactional
	public void resendVerification(String email, RequestMetadata metadata) {
		userRepository.findByEmail(normalizeEmail(email))
				.filter(UserAccount::isEnabled)
				.filter(user -> !user.isEmailVerified())
				.ifPresent(user -> accountTokenService.issueEmailVerification(user, metadata));
	}

	@Transactional
	public void requestPasswordReset(String email, RequestMetadata metadata) {
		userRepository.findByEmail(normalizeEmail(email))
				.filter(UserAccount::isEnabled)
				.ifPresent(user -> {
					accountTokenService.issuePasswordReset(user, metadata);
					audit(user.getEmail(), "password_reset_requested", metadata, Map.of("userId", user.getId().toString()));
				});
	}

	@Transactional
	public void resetPassword(String token, String newPassword, RequestMetadata metadata) {
		UserAccount user = accountTokenService.consumePasswordReset(token, metadata);
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		refreshTokenService.revokeAllForUser(user);
		audit(user.getEmail(), "password_reset_success", metadata, Map.of("sessionsRevoked", "true"));
	}

	@Transactional
	public AuthSession refresh(String refreshToken, RequestMetadata metadata) {
		RefreshTokenService.RefreshRotation rotation = refreshTokenService.rotate(refreshToken, metadata);
		return new AuthSession(
				rotation.accessToken(),
				rotation.refreshToken(),
				responseFor(rotation.user())
		);
	}

	@Transactional
	public void logout(String refreshToken, RequestMetadata metadata) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenService.revoke(refreshToken, metadata);
		}
	}

	private AuthSession sessionFor(UserAccount user, RequestMetadata metadata) {
		return new AuthSession(
				jwtService.createAccessToken(user),
				refreshTokenService.issue(user, metadata),
				responseFor(user)
		);
	}

	private AuthResponse responseFor(UserAccount user) {
		return new AuthResponse(
				jwtService.expiresInSeconds(),
				jwtService.refreshExpiresInSeconds(),
				UserProfileResponse.from(user)
		);
	}

	private ResponseStatusException invalidCredentials() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private void audit(String actor, String action, RequestMetadata metadata, Map<String, String> extraMetadata) {
		java.util.HashMap<String, String> values = new java.util.HashMap<>(extraMetadata);
		values.put("ipAddress", metadata.ipAddress());
		values.put("userAgent", metadata.userAgent());
		auditService.publish(new AuditEvent(
				actor,
				action,
				"auth.account",
				metadata.requestId(),
				Instant.now(),
				Map.copyOf(values)
		));
	}
}
