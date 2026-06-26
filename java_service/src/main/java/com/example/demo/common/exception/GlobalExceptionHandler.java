package com.example.demo.common.exception;

import com.example.demo.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final String REQUEST_ID_ATTRIBUTE = "requestId";

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<ApiError>> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		Map<String, String> fields = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
						(existing, replacement) -> existing
				));

		return ResponseEntity.badRequest().body(ApiResponse.error(
				"Validation failed",
				new ApiError("VALIDATION_ERROR", fields),
				requestId(request)
		));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<ApiError>> handleAccessDenied(
			AccessDeniedException exception,
			HttpServletRequest request
	) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(
				"Access denied",
				ApiError.of("ACCESS_DENIED"),
				requestId(request)
		));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<ApiError>> handleResponseStatus(
			ResponseStatusException exception,
			HttpServletRequest request
	) {
		return ResponseEntity.status(exception.getStatusCode()).body(ApiResponse.error(
				exception.getReason() == null ? "Request failed" : exception.getReason(),
				ApiError.of(exception.getStatusCode().toString()),
				requestId(request)
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<ApiError>> handleUnexpected(Exception exception, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
				"Unexpected server error",
				ApiError.of("INTERNAL_ERROR"),
				requestId(request)
		));
	}

	private String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute(REQUEST_ID_ATTRIBUTE);
		return requestId == null ? "unknown" : requestId.toString();
	}
}
