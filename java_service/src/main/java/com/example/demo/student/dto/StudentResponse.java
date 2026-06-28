package com.example.demo.student.dto;

import com.example.demo.student.Student;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudentResponse(
		UUID id,
		String fullName,
		String admissionNumber,
		String className,
		String sectionName,
		LocalDate dateOfBirth,
		String gender,
		String guardianName,
		String guardianPhone,
		String guardianEmail,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
	public static StudentResponse from(Student student) {
		return new StudentResponse(
				student.getId(),
				student.getFullName(),
				student.getAdmissionNumber(),
				student.getClassName(),
				student.getSectionName(),
				student.getDateOfBirth(),
				student.getGender(),
				student.getGuardianName(),
				student.getGuardianPhone(),
				student.getGuardianEmail(),
				student.getStatus(),
				student.getCreatedAt(),
				student.getUpdatedAt()
		);
	}
}