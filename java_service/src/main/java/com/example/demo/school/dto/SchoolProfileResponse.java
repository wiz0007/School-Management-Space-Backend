package com.example.demo.school.dto;

import com.example.demo.school.SchoolProfile;
import java.time.Instant;
import java.util.UUID;

public record SchoolProfileResponse(
		UUID id,
		String schoolName,
		String address,
		String contactEmail,
		String phone,
		String academicYear,
		String principalName,
		String timezone,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
	public static SchoolProfileResponse from(SchoolProfile profile) {
		return new SchoolProfileResponse(
				profile.getId(),
				profile.getSchoolName(),
				profile.getAddress(),
				profile.getContactEmail(),
				profile.getPhone(),
				profile.getAcademicYear(),
				profile.getPrincipalName(),
				profile.getTimezone(),
				profile.getStatus(),
				profile.getCreatedAt(),
				profile.getUpdatedAt()
		);
	}
}