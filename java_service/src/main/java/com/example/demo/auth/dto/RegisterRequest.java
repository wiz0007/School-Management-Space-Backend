package com.example.demo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
		@Size(min = 10, max = 72, message = "Password must be between 10 and 72 characters")
		@Pattern(
				regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
				message = "Password must include uppercase, lowercase, number, and special character"
		)
		String password
) {
}
