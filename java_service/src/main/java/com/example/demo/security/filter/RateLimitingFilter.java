package com.example.demo.security.filter;

import com.example.demo.security.ratelimit.FixedWindowRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
	private final FixedWindowRateLimiter rateLimiter;

	public RateLimitingFilter(FixedWindowRateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String key = clientIp(request) + ":" + request.getRequestURI();
		if (!rateLimiter.allow(key)) {
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String clientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
