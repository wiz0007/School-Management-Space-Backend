package com.example.demo.security;

import com.example.demo.user.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionRegistryTests {
	@Test
	void adminReceivesAllModulePermissions() {
		var authorities = RolePermissionRegistry.authoritiesFor(UserRole.ADMIN);

		for (AuthorizationPermission permission : AuthorizationPermission.values()) {
			assertTrue(
					authorities.stream().anyMatch(authority -> authority.getAuthority().equals(permission.authority())),
					"ADMIN should receive " + permission.authority()
			);
		}
		assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
	}

	@Test
	void teacherCanWriteAttendanceButCannotWriteStudentRecords() {
		var authorities = RolePermissionRegistry.authoritiesFor(UserRole.TEACHER);

		assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.ATTENDANCE_WRITE)));
		assertFalse(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.STUDENT_WRITE)));
		assertFalse(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.STAFF_WRITE)));
	}

	@Test
	void staffCanWriteOperationalClassesButCannotAdministerStaff() {
		var authorities = RolePermissionRegistry.authoritiesFor(UserRole.STAFF);

		assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.CLASS_WRITE)));
		assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.STUDENT_WRITE)));
		assertFalse(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.STAFF_WRITE)));
		assertFalse(authorities.stream().anyMatch(authority -> authority.getAuthority().equals(PermissionAuthority.SCHOOL_PROFILE_WRITE)));
	}
}
