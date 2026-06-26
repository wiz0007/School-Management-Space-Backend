package com.example.demo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Full name is required")
		@Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
		String fullName,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 180, message = "Email is too long")
		String email,

		@NotBlank(message = "Password is required")
		@Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
		String password
) {
}
