package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
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

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		UserAccount user = new UserAccount();
		user.setFullName(request.fullName().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));

		UserAccount savedUser = userRepository.save(user);
		return responseFor(savedUser);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		UserAccount user = userRepository.findByEmail(normalizeEmail(request.email()))
				.filter(UserAccount::isEnabled)
				.orElseThrow(() -> invalidCredentials());

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw invalidCredentials();
		}

		return responseFor(user);
	}

	private AuthResponse responseFor(UserAccount user) {
		return new AuthResponse(
				jwtService.createAccessToken(user),
				"Bearer",
				jwtService.expiresInSeconds(),
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
