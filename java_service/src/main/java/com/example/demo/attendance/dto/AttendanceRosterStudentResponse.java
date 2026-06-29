package com.example.demo.attendance.dto;

import com.example.demo.attendance.AttendanceStatus;
import com.example.demo.student.Student;
import java.util.UUID;

public record AttendanceRosterStudentResponse(
		UUID studentId,
		String studentName,
		String admissionNumber,
		AttendanceStatus status,
		String remarks
) {
	public static AttendanceRosterStudentResponse empty(Student student) {
		return new AttendanceRosterStudentResponse(
				student.getId(),
				student.getFullName(),
				student.getAdmissionNumber(),
				AttendanceStatus.PRESENT,
				null
		);
	}
}