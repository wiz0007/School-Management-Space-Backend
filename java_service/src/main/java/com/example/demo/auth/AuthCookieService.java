package com.example.demo.auth;

import com.example.demo.config.SecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {
	private final SecurityProperties properties;

	public AuthCookieService(SecurityProperties properties) {
		this.properties = properties;
	}

	public void attachSessionCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookie(properties.authCookieName(), accessToken, properties.jwtExpiration()).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, cookie(properties.refreshCookieName(), refreshToken, properties.jwtRefreshExpiration()).toString());
	}

	public void clearSessionCookies(HttpServletResponse response) {
		response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(properties.authCookieName()).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(properties.refreshCookieName()).toString());
	}

	public Optional<String> refreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
				.filter(cookie -> properties.refreshCookieName().equals(cookie.getName()))
				.map(Cookie::getValue)
				.findFirst();
	}

	private ResponseCookie cookie(String name, String value, java.time.Duration maxAge) {
		return ResponseCookie.from(name, value)
				.httpOnly(true)
				.secure(properties.authCookieSecure())
				.sameSite("Strict")
				.path("/")
				.maxAge(maxAge)
				.build();
	}

	private ResponseCookie expiredCookie(String name) {
		return ResponseCookie.from(name, "")
				.httpOnly(true)
				.secure(properties.authCookieSecure())
				.sameSite("Strict")
				.path("/")
				.maxAge(0)
				.build();
	}
}