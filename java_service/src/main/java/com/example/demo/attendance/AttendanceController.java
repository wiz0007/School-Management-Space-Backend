package com.example.demo.attendance;

import com.example.demo.attendance.dto.AttendanceRecordResponse;
import com.example.demo.attendance.dto.AttendanceRosterStudentResponse;
import com.example.demo.attendance.dto.AttendanceSaveRequest;
import com.example.demo.common.api.ApiResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
	private final AttendanceService attendanceService;

	public AttendanceController(AttendanceService attendanceService) {
		this.attendanceService = attendanceService;
	}

	@GetMapping("/roster")
	public ApiResponse<List<AttendanceRosterStudentResponse>> roster(
			@AuthenticationPrincipal UserAccount user,
			@RequestParam UUID classId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Attendance roster loaded", attendanceService.roster(user, classId, attendanceDate), requestId(request));
	}

	@GetMapping
	public ApiResponse<List<AttendanceRecordResponse>> records(
			@AuthenticationPrincipal UserAccount user,
			@RequestParam UUID classId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Attendance records loaded", attendanceService.records(user, classId, attendanceDate), requestId(request));
	}

	@PostMapping
	public ApiResponse<List<AttendanceRecordResponse>> save(
			@AuthenticationPrincipal UserAccount user,
			@Valid @RequestBody AttendanceSaveRequest saveRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Attendance saved", attendanceService.save(user, saveRequest), requestId(request));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}