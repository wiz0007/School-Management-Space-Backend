package com.example.demo.staff;

import com.example.demo.common.api.ApiResponse;
import com.example.demo.staff.dto.StaffRequest;
import com.example.demo.staff.dto.StaffResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {
	private final StaffService staffService;

	public StaffController(StaffService staffService) {
		this.staffService = staffService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).STAFF_READ)")
	public ApiResponse<List<StaffResponse>> list(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Staff loaded", staffService.list(user), requestId(request));
	}

	@PostMapping
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).STAFF_WRITE)")
	public ApiResponse<StaffResponse> create(
			@AuthenticationPrincipal UserAccount user,
			@Valid @RequestBody StaffRequest staffRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Staff member created", staffService.create(user, staffRequest), requestId(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).STAFF_WRITE)")
	public ApiResponse<StaffResponse> update(
			@AuthenticationPrincipal UserAccount user,
			@PathVariable UUID id,
			@Valid @RequestBody StaffRequest staffRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok("Staff member updated", staffService.update(user, id, staffRequest), requestId(request));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}
