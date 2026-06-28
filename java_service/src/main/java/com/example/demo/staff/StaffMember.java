package com.example.demo.staff;

import com.example.demo.school.SchoolProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
		name = "staff_members",
		indexes = {
				@Index(name = "idx_staff_school", columnList = "school_profile_id"),
				@Index(name = "idx_staff_employee_code", columnList = "school_profile_id,employeeCode", unique = true)
		}
)
public class StaffMember {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_profile_id", nullable = false)
	private SchoolProfile schoolProfile;

	@Column(nullable = false, length = 120)
	private String fullName;

	@Column(nullable = false, length = 40)
	private String employeeCode;

	@Column(nullable = false, length = 80)
	private String role;

	@Column(nullable = false, length = 180)
	private String email;

	@Column(nullable = false, length = 40)
	private String phone;

	@Column(nullable = false, length = 100)
	private String department;

	@Column(nullable = false)
	private LocalDate joiningDate;

	@Column(nullable = false, length = 40)
	private String status = "ACTIVE";

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		normalize();
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
		normalize();
	}

	public UUID getId() { return id; }
	public SchoolProfile getSchoolProfile() { return schoolProfile; }
	public void setSchoolProfile(SchoolProfile schoolProfile) { this.schoolProfile = schoolProfile; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = clean(fullName); }
	public String getEmployeeCode() { return employeeCode; }
	public void setEmployeeCode(String employeeCode) { this.employeeCode = clean(employeeCode); }
	public String getRole() { return role; }
	public void setRole(String role) { this.role = clean(role); }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = normalizeEmail(email); }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = clean(phone); }
	public String getDepartment() { return department; }
	public void setDepartment(String department) { this.department = clean(department); }
	public LocalDate getJoiningDate() { return joiningDate; }
	public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = clean(status); }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	private void normalize() {
		fullName = clean(fullName);
		employeeCode = clean(employeeCode);
		role = clean(role);
		email = normalizeEmail(email);
		phone = clean(phone);
		department = clean(department);
		status = status == null || status.isBlank() ? "ACTIVE" : clean(status).toUpperCase();
	}

	private String clean(String value) { return value == null ? null : value.trim(); }
	private String normalizeEmail(String value) { return value == null ? null : value.trim().toLowerCase(); }
}