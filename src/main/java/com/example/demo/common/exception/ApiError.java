package com.example.demo.common.exception;

import java.util.Map;

public record ApiError(
		String code,
		Map<String, String> details
) {
	public static ApiError of(String code) {
		return new ApiError(code, Map.of());
	}
}
