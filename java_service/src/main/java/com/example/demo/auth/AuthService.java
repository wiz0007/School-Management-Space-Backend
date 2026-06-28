package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.AuthSession;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.security.JwtService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRepository;
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

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			RefreshTokenService refreshTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public AuthSession register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		UserAccount user = new UserAccount();
		user.setFullName(request.fullName().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));

		UserAccount savedUser = userRepository.save(user);
		return sessionFor(savedUser);
	}

	@Transactional(readOnly = true)
	public AuthSession login(LoginRequest request) {
		UserAccount user = userRepository.findByEmail(normalizeEmail(request.email()))
				.filter(UserAccount::isEnabled)
				.orElseThrow(() -> invalidCredentials());

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw invalidCredentials();
		}

		return sessionFor(user);
	}

	@Transactional
	public AuthSession refresh(String refreshToken) {
		RefreshTokenService.RefreshRotation rotation = refreshTokenService.rotate(refreshToken);
		return new AuthSession(
				rotation.accessToken(),
				rotation.refreshToken(),
				responseFor(rotation.user())
		);
	}

	@Transactional
	public void logout(String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenService.revoke(refreshToken);
		}
	}

	private AuthSession sessionFor(UserAccount user) {
		return new AuthSession(
				jwtService.createAccessToken(user),
				refreshTokenService.issue(user),
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
}