package com.example.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
		@NotBlank(message = "Reset token is required")
		@Size(min = 32, max = 200, message = "Reset token is invalid")
		String token,

		@NotBlank(message = "Password is required")
		@Size(min = 10, max = 72, message = "Password must be between 10 and 72 characters")
		@Pattern(
				regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
				message = "Password must include uppercase, lowercase, number, and special character"
		)
		String password
) {
}
