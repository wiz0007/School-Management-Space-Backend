package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.AuthSession;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.common.api.ApiResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;
	private final AuthCookieService authCookieService;

	public AuthController(AuthService authService, AuthCookieService authCookieService) {
		this.authService = authService;
		this.authCookieService = authCookieService;
	}

	@PostMapping("/register")
	public ApiResponse<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		AuthSession session = authService.register(request);
		authCookieService.attachSessionCookies(httpResponse, session.accessToken(), session.refreshToken());
		return ApiResponse.ok("Registration successful", session.response(), requestId(httpRequest));
	}

	@PostMapping("/login")
	public ApiResponse<AuthResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		AuthSession session = authService.login(request);
		authCookieService.attachSessionCookies(httpResponse, session.accessToken(), session.refreshToken());
		return ApiResponse.ok("Login successful", session.response(), requestId(httpRequest));
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = authCookieService.refreshToken(request)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh session is missing"));
		AuthSession session = authService.refresh(refreshToken);
		authCookieService.attachSessionCookies(response, session.accessToken(), session.refreshToken());
		return ApiResponse.ok("Session refreshed", session.response(), requestId(request));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authCookieService.refreshToken(request).ifPresent(authService::logout);
		authCookieService.clearSessionCookies(response);
		return ApiResponse.ok("Logout successful", null, requestId(request));
	}

	@GetMapping("/me")
	public ApiResponse<UserProfileResponse> me(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest httpRequest
	) {
		return ApiResponse.ok("Current user loaded", UserProfileResponse.from(user), requestId(httpRequest));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}