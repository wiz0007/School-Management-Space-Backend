package com.example.demo.health;

import com.example.demo.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
	@GetMapping
	public ApiResponse<HealthStatus> health(HttpServletRequest request) {
		return ApiResponse.ok(
				"Service healthy",
				new HealthStatus("school-management-api", "UP", Instant.now()),
				request.getAttribute("requestId").toString()
		);
	}
}
