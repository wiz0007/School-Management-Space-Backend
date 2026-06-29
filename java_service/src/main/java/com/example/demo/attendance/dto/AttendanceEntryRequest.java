package com.example.demo.attendance.dto;

import com.example.demo.attendance.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AttendanceEntryRequest(
		@NotNull(message = "Student id is required")
		UUID studentId,

		@NotNull(message = "Attendance status is required")
		AttendanceStatus status,

		@Size(max = 240, message = "Remarks must be 240 characters or fewer")
		String remarks
) {
}