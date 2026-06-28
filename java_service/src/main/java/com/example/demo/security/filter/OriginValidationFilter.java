package com.example.demo.security.filter;

import com.example.demo.config.CorsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OriginValidationFilter extends OncePerRequestFilter {
	private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
	private final CorsProperties corsProperties;

	public OriginValidationFilter(CorsProperties corsProperties) {
		this.corsProperties = corsProperties;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api/") || SAFE_METHODS.contains(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		String origin = request.getHeader("Origin");
		String referer = request.getHeader("Referer");
		if (isAllowed(origin) || (origin == null && (referer == null || isAllowedReferer(referer)))) {
			filterChain.doFilter(request, response);
			return;
		}

		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Request origin is not allowed");
	}

	private boolean isAllowed(String origin) {
		return origin != null && corsProperties.allowedOrigins().contains(origin);
	}

	private boolean isAllowedReferer(String referer) {
		return corsProperties.allowedOrigins().stream().anyMatch(referer::startsWith);
	}
}
