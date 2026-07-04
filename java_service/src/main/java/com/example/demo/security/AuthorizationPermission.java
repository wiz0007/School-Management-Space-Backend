package com.example.demo.security;

public enum AuthorizationPermission {
	SCHOOL_PROFILE_READ("school.profile:read"),
	SCHOOL_PROFILE_WRITE("school.profile:write"),
	STUDENT_READ("student:read"),
	STUDENT_WRITE("student:write"),
	STAFF_READ("staff:read"),
	STAFF_WRITE("staff:write"),
	CLASS_READ("class:read"),
	CLASS_WRITE("class:write"),
	ATTENDANCE_READ("attendance:read"),
	ATTENDANCE_WRITE("attendance:write"),
	AUDIT_READ("audit:read");

	private final String authority;

	AuthorizationPermission(String authority) {
		this.authority = authority;
	}

	public String authority() {
		return authority;
	}
}
