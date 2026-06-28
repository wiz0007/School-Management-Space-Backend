package com.example.demo.school.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SchoolProfileRequest(
		@NotBlank(message = "School name is required")
		@Size(max = 160, message = "School name must be 160 characters or fewer")
		String schoolName,

		@NotBlank(message = "Address is required")
		@Size(max = 260, message = "Address must be 260 characters or fewer")
		String address,

		@NotBlank(message = "Contact email is required")
		@Email(message = "Contact email must be valid")
		@Size(max = 180, message = "Contact email must be 180 characters or fewer")
		String contactEmail,

		@NotBlank(message = "Phone is required")
		@Pattern(regexp = "^[0-9+()\\-\\s]{7,40}$", message = "Phone number contains unsupported characters")
		String phone,

		@NotBlank(message = "Academic year is required")
		@Size(max = 40, message = "Academic year must be 40 characters or fewer")
		String academicYear,

		@NotBlank(message = "Principal name is required")
		@Size(max = 120, message = "Principal name must be 120 characters or fewer")
		String principalName,

		@NotBlank(message = "Timezone is required")
		@Size(max = 80, message = "Timezone must be 80 characters or fewer")
		String timezone,

		@Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
		String status
) {
}