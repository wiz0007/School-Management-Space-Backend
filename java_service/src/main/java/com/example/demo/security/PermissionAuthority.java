package com.example.demo.security;

public final class PermissionAuthority {
	public static final String SCHOOL_PROFILE_READ = "school.profile:read";
	public static final String SCHOOL_PROFILE_WRITE = "school.profile:write";
	public static final String STUDENT_READ = "student:read";
	public static final String STUDENT_WRITE = "student:write";
	public static final String STAFF_READ = "staff:read";
	public static final String STAFF_WRITE = "staff:write";
	public static final String CLASS_READ = "class:read";
	public static final String CLASS_WRITE = "class:write";
	public static final String ATTENDANCE_READ = "attendance:read";
	public static final String ATTENDANCE_WRITE = "attendance:write";
	public static final String AUDIT_READ = "audit:read";

	private PermissionAuthority() {
	}
}
