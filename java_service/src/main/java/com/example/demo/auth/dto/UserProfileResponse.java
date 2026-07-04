package com.example.demo.auth.dto;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
		UUID id,
		String fullName,
		String email,
		UserRole role,
		boolean emailVerified,
		Instant emailVerifiedAt,
		Instant createdAt
) {
	public static UserProfileResponse from(UserAccount user) {
		return new UserProfileResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getRole(),
				user.isEmailVerified(),
				user.getEmailVerifiedAt(),
				user.getCreatedAt()
		);
	}
}
