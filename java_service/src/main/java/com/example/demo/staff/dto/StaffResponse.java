package com.example.demo.staff.dto;

import com.example.demo.staff.StaffMember;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StaffResponse(
		UUID id,
		String fullName,
		String employeeCode,
		String role,
		String email,
		String phone,
		String department,
		LocalDate joiningDate,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
	public static StaffResponse from(StaffMember staff) {
		return new StaffResponse(
				staff.getId(),
				staff.getFullName(),
				staff.getEmployeeCode(),
				staff.getRole(),
				staff.getEmail(),
				staff.getPhone(),
				staff.getDepartment(),
				staff.getJoiningDate(),
				staff.getStatus(),
				staff.getCreatedAt(),
				staff.getUpdatedAt()
		);
	}
}