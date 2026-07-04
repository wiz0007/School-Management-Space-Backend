package com.example.demo.security;

import com.example.demo.user.UserRole;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class RolePermissionRegistry {
	private static final EnumMap<UserRole, Set<AuthorizationPermission>> ROLE_PERMISSIONS = new EnumMap<>(UserRole.class);

	static {
		ROLE_PERMISSIONS.put(UserRole.ADMIN, EnumSet.allOf(AuthorizationPermission.class));
		ROLE_PERMISSIONS.put(UserRole.TEACHER, EnumSet.of(
				AuthorizationPermission.SCHOOL_PROFILE_READ,
				AuthorizationPermission.STUDENT_READ,
				AuthorizationPermission.CLASS_READ,
				AuthorizationPermission.ATTENDANCE_READ,
				AuthorizationPermission.ATTENDANCE_WRITE
		));
		ROLE_PERMISSIONS.put(UserRole.STAFF, EnumSet.of(
				AuthorizationPermission.SCHOOL_PROFILE_READ,
				AuthorizationPermission.STUDENT_READ,
				AuthorizationPermission.STUDENT_WRITE,
				AuthorizationPermission.STAFF_READ,
				AuthorizationPermission.CLASS_READ,
				AuthorizationPermission.CLASS_WRITE,
				AuthorizationPermission.ATTENDANCE_READ
		));
	}

	private RolePermissionRegistry() {
	}

	public static Set<SimpleGrantedAuthority> authoritiesFor(UserRole role) {
		Set<SimpleGrantedAuthority> authorities = new java.util.HashSet<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
		ROLE_PERMISSIONS.getOrDefault(role, Set.of()).stream()
				.map(AuthorizationPermission::authority)
				.map(SimpleGrantedAuthority::new)
				.forEach(authorities::add);
		return Set.copyOf(authorities);
	}
}
