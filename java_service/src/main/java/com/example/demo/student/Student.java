package com.example.demo.student;

import com.example.demo.school.SchoolProfile;
import com.example.demo.schoolclass.SchoolClass;
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
		name = "students",
		indexes = {
				@Index(name = "idx_students_school", columnList = "school_profile_id"),
				@Index(name = "idx_students_class", columnList = "school_class_id"),
				@Index(name = "idx_students_admission_number", columnList = "school_profile_id,admissionNumber", unique = true)
		}
)
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_profile_id", nullable = false)
	private SchoolProfile schoolProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "school_class_id")
	private SchoolClass assignedClass;

	@Column(nullable = false, length = 120)
	private String fullName;

	@Column(nullable = false, length = 40)
	private String admissionNumber;

	@Column(nullable = false, length = 80)
	private String className;

	@Column(nullable = false, length = 80)
	private String sectionName;

	@Column(nullable = false)
	private LocalDate dateOfBirth;

	@Column(nullable = false, length = 20)
	private String gender;

	@Column(nullable = false, length = 120)
	private String guardianName;

	@Column(nullable = false, length = 40)
	private String guardianPhone;

	@Column(length = 180)
	private String guardianEmail;

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
	public SchoolClass getAssignedClass() { return assignedClass; }
	public void setAssignedClass(SchoolClass assignedClass) { this.assignedClass = assignedClass; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = clean(fullName); }
	public String getAdmissionNumber() { return admissionNumber; }
	public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = clean(admissionNumber); }
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = clean(className); }
	public String getSectionName() { return sectionName; }
	public void setSectionName(String sectionName) { this.sectionName = clean(sectionName); }
	public LocalDate getDateOfBirth() { return dateOfBirth; }
	public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = clean(gender); }
	public String getGuardianName() { return guardianName; }
	public void setGuardianName(String guardianName) { this.guardianName = clean(guardianName); }
	public String getGuardianPhone() { return guardianPhone; }
	public void setGuardianPhone(String guardianPhone) { this.guardianPhone = clean(guardianPhone); }
	public String getGuardianEmail() { return guardianEmail; }
	public void setGuardianEmail(String guardianEmail) { this.guardianEmail = normalizeEmail(guardianEmail); }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = clean(status); }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	private void normalize() {
		fullName = clean(fullName);
		admissionNumber = clean(admissionNumber);
		className = clean(className);
		sectionName = clean(sectionName);
		gender = clean(gender);
		guardianName = clean(guardianName);
		guardianPhone = clean(guardianPhone);
		guardianEmail = normalizeEmail(guardianEmail);
		status = status == null || status.isBlank() ? "ACTIVE" : clean(status).toUpperCase();
	}

	private String clean(String value) { return value == null ? null : value.trim(); }
	private String normalizeEmail(String value) { return value == null || value.isBlank() ? null : value.trim().toLowerCase(); }
}