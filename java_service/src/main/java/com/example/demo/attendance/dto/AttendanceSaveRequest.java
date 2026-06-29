package com.example.demo.attendance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AttendanceSaveRequest(
		@NotNull(message = "Class id is required")
		UUID classId,

		@NotNull(message = "Attendance date is required")
		LocalDate attendanceDate,

		@NotEmpty(message = "At least one attendance entry is required")
		List<@Valid AttendanceEntryRequest> entries
) {
}