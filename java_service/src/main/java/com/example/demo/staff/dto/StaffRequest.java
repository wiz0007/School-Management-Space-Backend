package com.example.demo.staff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StaffRequest(
		@NotBlank(message = "Staff name is required")
		@Size(max = 120, message = "Staff name must be 120 characters or fewer")
		String fullName,

		@NotBlank(message = "Employee code is required")
		@Size(max = 40, message = "Employee code must be 40 characters or fewer")
		String employeeCode,

		@NotBlank(message = "Role is required")
		@Size(max = 80, message = "Role must be 80 characters or fewer")
		String role,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 180, message = "Email must be 180 characters or fewer")
		String email,

		@NotBlank(message = "Phone is required")
		@Pattern(regexp = "^[0-9+()\\-\\s]{7,40}$", message = "Phone contains unsupported characters")
		String phone,

		@NotBlank(message = "Department is required")
		@Size(max = 100, message = "Department must be 100 characters or fewer")
		String department,

		@NotNull(message = "Joining date is required")
		LocalDate joiningDate,

		@Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
		String status
) {
}