package com.example.demo.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record StudentRequest(
		@NotBlank(message = "Student name is required")
		@Size(max = 120, message = "Student name must be 120 characters or fewer")
		String fullName,

		@NotBlank(message = "Admission number is required")
		@Size(max = 40, message = "Admission number must be 40 characters or fewer")
		String admissionNumber,

		UUID classId,

		@NotBlank(message = "Class is required")
		@Size(max = 80, message = "Class must be 80 characters or fewer")
		String className,

		@NotBlank(message = "Section is required")
		@Size(max = 80, message = "Section must be 80 characters or fewer")
		String sectionName,

		@NotNull(message = "Date of birth is required")
		@Past(message = "Date of birth must be in the past")
		LocalDate dateOfBirth,

		@NotBlank(message = "Gender is required")
		@Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
		String gender,

		@NotBlank(message = "Guardian name is required")
		@Size(max = 120, message = "Guardian name must be 120 characters or fewer")
		String guardianName,

		@NotBlank(message = "Guardian phone is required")
		@Pattern(regexp = "^[0-9+()\\-\\s]{7,40}$", message = "Guardian phone contains unsupported characters")
		String guardianPhone,

		@Email(message = "Guardian email must be valid")
		@Size(max = 180, message = "Guardian email must be 180 characters or fewer")
		String guardianEmail,

		@Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
		String status
) {
}