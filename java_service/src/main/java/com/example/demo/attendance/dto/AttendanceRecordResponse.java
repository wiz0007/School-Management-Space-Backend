package com.example.demo.attendance.dto;

import com.example.demo.attendance.AttendanceRecord;
import com.example.demo.attendance.AttendanceStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordResponse(
		UUID id,
		UUID studentId,
		String studentName,
		String admissionNumber,
		UUID classId,
		String classDisplayName,
		LocalDate attendanceDate,
		AttendanceStatus status,
		String remarks,
		Instant updatedAt
) {
	public static AttendanceRecordResponse from(AttendanceRecord record) {
		return new AttendanceRecordResponse(
				record.getId(),
				record.getStudent().getId(),
				record.getStudent().getFullName(),
				record.getStudent().getAdmissionNumber(),
				record.getSchoolClass().getId(),
				record.getSchoolClass().getClassName() + " " + record.getSchoolClass().getSectionName(),
				record.getAttendanceDate(),
				record.getStatus(),
				record.getRemarks(),
				record.getUpdatedAt()
		);
	}
}