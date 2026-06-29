package com.example.demo.schoolclass;

import com.example.demo.common.api.ApiResponse;
import com.example.demo.schoolclass.dto.SchoolClassRequest;
import com.example.demo.schoolclass.dto.SchoolClassResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/classes")
public class SchoolClassController {
	private final SchoolClassService schoolClassService;

	public SchoolClassController(SchoolClassService schoolClassService) {
		this.schoolClassService = schoolClassService;
	}

	@GetMapping
	public ApiResponse<List<SchoolClassResponse>> list(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Classes loaded", schoolClassService.list(user), requestId(request));
	}

	@PostMapping
	public ApiResponse<SchoolClassResponse> create(
			@AuthenticationPrincipal UserAccount user,
			@Valid @RequestBody SchoolClassRequest classRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Class created", schoolClassService.create(user, classRequest), requestId(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<SchoolClassResponse> update(
			@AuthenticationPrincipal UserAccount user,
			@PathVariable UUID id,
			@Valid @RequestBody SchoolClassRequest classRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Class updated", schoolClassService.update(user, id, classRequest), requestId(request));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}