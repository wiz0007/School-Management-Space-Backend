package com.example.demo.security.filter;

import com.example.demo.security.JwtService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final String BEARER_PREFIX = "Bearer ";
	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			String token = authorization.substring(BEARER_PREFIX.length());
			jwtService.subject(token)
					.flatMap(userRepository::findByEmail)
					.filter(UserAccount::isEnabled)
					.ifPresent(this::authenticate);
		}

		filterChain.doFilter(request, response);
	}

	private void authenticate(UserAccount user) {
		List<SimpleGrantedAuthority> authorities = List.of(
				new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
		);
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(user, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
