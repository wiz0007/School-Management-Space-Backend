package com.example.demo.schoolclass.dto;

import com.example.demo.schoolclass.SchoolClass;
import java.time.Instant;
import java.util.UUID;

public record SchoolClassResponse(
		UUID id,
		String className,
		String sectionName,
		String academicYear,
		UUID classTeacherId,
		String classTeacherName,
		int capacity,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
	public static SchoolClassResponse from(SchoolClass schoolClass) {
		return new SchoolClassResponse(
				schoolClass.getId(),
				schoolClass.getClassName(),
				schoolClass.getSectionName(),
				schoolClass.getAcademicYear(),
				schoolClass.getClassTeacher() == null ? null : schoolClass.getClassTeacher().getId(),
				schoolClass.getClassTeacher() == null ? null : schoolClass.getClassTeacher().getFullName(),
				schoolClass.getCapacity(),
				schoolClass.getStatus(),
				schoolClass.getCreatedAt(),
				schoolClass.getUpdatedAt()
		);
	}
}