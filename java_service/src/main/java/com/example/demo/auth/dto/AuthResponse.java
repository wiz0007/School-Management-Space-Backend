package com.example.demo.auth.dto;

public record AuthResponse(
		long accessExpiresInSeconds,
		long refreshExpiresInSeconds,
		UserProfileResponse user
) {
}