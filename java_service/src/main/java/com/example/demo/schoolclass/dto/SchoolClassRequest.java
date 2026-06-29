package com.example.demo.schoolclass.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SchoolClassRequest(
		@NotBlank(message = "Class name is required")
		@Size(max = 80, message = "Class name must be 80 characters or fewer")
		String className,

		@NotBlank(message = "Section is required")
		@Size(max = 80, message = "Section must be 80 characters or fewer")
		String sectionName,

		@NotBlank(message = "Academic year is required")
		@Size(max = 40, message = "Academic year must be 40 characters or fewer")
		String academicYear,

		UUID classTeacherId,

		@Min(value = 1, message = "Capacity must be at least 1")
		@Max(value = 500, message = "Capacity must be 500 or fewer")
		int capacity,

		@Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
		String status
) {
}