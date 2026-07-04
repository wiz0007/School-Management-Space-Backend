package com.example.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
		@NotBlank(message = "Verification token is required")
		@Size(min = 32, max = 200, message = "Verification token is invalid")
		String token
) {
}
