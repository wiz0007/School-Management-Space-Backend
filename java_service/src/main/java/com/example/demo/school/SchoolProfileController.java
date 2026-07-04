package com.example.demo.school;

import com.example.demo.common.api.ApiResponse;
import com.example.demo.school.dto.SchoolProfileRequest;
import com.example.demo.school.dto.SchoolProfileResponse;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school/profile")
public class SchoolProfileController {
	private final SchoolProfileService schoolProfileService;

	public SchoolProfileController(SchoolProfileService schoolProfileService) {
		this.schoolProfileService = schoolProfileService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).SCHOOL_PROFILE_READ)")
	public ApiResponse<SchoolProfileResponse> getProfile(
			@AuthenticationPrincipal UserAccount user,
			HttpServletRequest request
	) {
		return ApiResponse.ok(
				"School profile loaded",
				schoolProfileService.currentProfile(user).orElse(null),
				requestId(request)
		);
	}

	@PutMapping
	@PreAuthorize("hasAuthority(T(com.example.demo.security.PermissionAuthority).SCHOOL_PROFILE_WRITE)")
	public ApiResponse<SchoolProfileResponse> saveProfile(
			@AuthenticationPrincipal UserAccount user,
			@Valid @RequestBody SchoolProfileRequest profileRequest,
			HttpServletRequest request
	) {
		return ApiResponse.ok(
				"School profile saved",
				schoolProfileService.save(user, profileRequest),
				requestId(request)
		);
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}
}
