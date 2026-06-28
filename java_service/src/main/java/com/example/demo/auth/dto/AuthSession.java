package com.example.demo.auth.dto;

public record AuthSession(
		String accessToken,
		String refreshToken,
		AuthResponse response
) {
}