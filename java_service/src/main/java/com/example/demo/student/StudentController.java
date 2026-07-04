package com.example.demo.student;

import com.example.demo.common.api.ApiResponse;
import com.example.demo.student.dto.StudentRequest;
import com.example.demo.student.dto.StudentResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {
	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).STUDENT_READ)")
	public ApiResponse<List<StudentResponse>> list(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Students loaded", studentService.list(user), requestId(request));
	}

	@PostMapping
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).STUDENT_WRITE)")
	public ApiResponse<StudentResponse> create(
			@AuthenticationPrincipal UserAccount user,
			@Valid @RequestBody StudentRequest studentRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Student created", studentService.create(user, studentRequest), requestId(request));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}
