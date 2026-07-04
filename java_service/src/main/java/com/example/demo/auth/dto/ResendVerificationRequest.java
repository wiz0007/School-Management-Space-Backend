package com.example.demo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendVerificationRequest(
		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 180, message = "Email is too long")
		String email
) {
}
