package com.example.demo.auth;

import jakarta.servlet.http.HttpServletRequest;

public record RequestMetadata(String ipAddress, String userAgent, String requestId) {
	private static final int MAX_USER_AGENT_LENGTH = 512;
	private static final int MAX_IP_LENGTH = 64;

	public static RequestMetadata from(HttpServletRequest request) {
		return new RequestMetadata(
				truncate(clientIp(request), MAX_IP_LENGTH),
				truncate(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH),
				requestId(request)
		);
	}

	private static String clientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute("requestId");
		return requestId == null ? "unknown" : requestId.toString();
	}

	private static String truncate(String value, int maxLength) {
		if (value == null || value.isBlank()) {
			return "unknown";
		}
		return value.length() <= maxLength ? value : value.substring(0, maxLength);
	}
}