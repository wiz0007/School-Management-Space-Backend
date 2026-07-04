package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.AuthSession;
import com.example.demo.auth.dto.ForgotPasswordRequest;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.auth.dto.ResendVerificationRequest;
import com.example.demo.auth.dto.ResetPasswordRequest;
import com.example.demo.auth.dto.SessionResponse;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.auth.dto.VerifyEmailRequest;
import com.example.demo.common.api.ApiResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	private final SessionManagementService sessionManagementService;

	public AuthController(
			AuthService authService,
			AuthCookieService authCookieService,
			SessionManagementService sessionManagementService
	) {
		this.authService = authService;
		this.authCookieService = authCookieService;
		this.sessionManagementService = sessionManagementService;
	}

	@PostMapping("/register")
	public ApiResponse<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		AuthSession session = authService.register(request, RequestMetadata.from(httpRequest));
		authCookieService.attachSessionCookies(httpResponse, session.accessToken(), session.refreshToken());
		return ApiResponse.ok("Registration successful. Please verify your email.", session.response(), requestId(httpRequest));
	}

	@PostMapping("/login")
	public ApiResponse<AuthResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		AuthSession session = authService.login(request, RequestMetadata.from(httpRequest));
		authCookieService.attachSessionCookies(httpResponse, session.accessToken(), session.refreshToken());
		return ApiResponse.ok("Login successful", session.response(), requestId(httpRequest));
	}

	@PostMapping("/verify-email")
	public ApiResponse<UserProfileResponse> verifyEmail(
			@Valid @RequestBody VerifyEmailRequest request,
			HttpServletRequest httpRequest
	) {
		UserProfileResponse user = authService.verifyEmail(request.token(), RequestMetadata.from(httpRequest));
		return ApiResponse.ok("Email verified", user, requestId(httpRequest));
	}

	@PostMapping("/resend-verification")
	public ApiResponse<Void> resendVerification(
			@Valid @RequestBody ResendVerificationRequest request,
			HttpServletRequest httpRequest
	) {
		authService.resendVerification(request.email(), RequestMetadata.from(httpRequest));
		return ApiResponse.ok("If the account needs verification, a new link will be sent.", null, requestId(httpRequest));
	}

	@PostMapping("/forgot-password")
	public ApiResponse<Void> forgotPassword(
			@Valid @RequestBody ForgotPasswordRequest request,
			HttpServletRequest httpRequest
	) {
		authService.requestPasswordReset(request.email(), RequestMetadata.from(httpRequest));
		return ApiResponse.ok("If the account exists, a password reset link will be sent.", null, requestId(httpRequest));
	}

	@PostMapping("/reset-password")
	public ApiResponse<Void> resetPassword(
			@Valid @RequestBody ResetPasswordRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		authService.resetPassword(request.token(), request.password(), RequestMetadata.from(httpRequest));
		authCookieService.clearSessionCookies(httpResponse);
		return ApiResponse.ok("Password reset successful. Please sign in again.", null, requestId(httpRequest));
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = authCookieService.refreshToken(request)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh session is missing"));
		AuthSession session = authService.refresh(refreshToken, RequestMetadata.from(request));
		authCookieService.attachSessionCookies(response, session.accessToken(), session.refreshToken());
		return ApiResponse.ok("Session refreshed", session.response(), requestId(request));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authCookieService.refreshToken(request).ifPresent(token -> authService.logout(token, RequestMetadata.from(request)));
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

	@GetMapping("/sessions")
	public ApiResponse<List<SessionResponse>> sessions(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest httpRequest
	) {
		List<SessionResponse> sessions = sessionManagementService.activeSessions(
				user,
				authCookieService.refreshToken(httpRequest)
		);
		return ApiResponse.ok("Active sessions loaded", sessions, requestId(httpRequest));
	}

	@DeleteMapping("/sessions/{sessionId}")
	public ApiResponse<Void> revokeSession(
			@PathVariable UUID sessionId,
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		boolean currentSession = sessionManagementService.revokeSession(
				user,
				sessionId,
				authCookieService.refreshToken(httpRequest),
				RequestMetadata.from(httpRequest)
		);
		if (currentSession) {
			authCookieService.clearSessionCookies(httpResponse);
		}
		return ApiResponse.ok("Session revoked", null, requestId(httpRequest));
	}

	@DeleteMapping("/sessions/others")
	public ApiResponse<Void> revokeOtherSessions(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest httpRequest
	) {
		int revoked = sessionManagementService.revokeOtherSessions(
				user,
				authCookieService.refreshToken(httpRequest),
				RequestMetadata.from(httpRequest)
		);
		return ApiResponse.ok("Other sessions revoked: " + revoked, null, requestId(httpRequest));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}
