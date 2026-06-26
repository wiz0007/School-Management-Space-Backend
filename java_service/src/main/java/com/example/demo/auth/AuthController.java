package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.common.api.ApiResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ApiResponse<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest
	) {
		return ApiResponse.ok("Registration successful", authService.register(request), requestId(httpRequest));
	}

	@PostMapping("/login")
	public ApiResponse<AuthResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest
	) {
		return ApiResponse.ok("Login successful", authService.login(request), requestId(httpRequest));
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
