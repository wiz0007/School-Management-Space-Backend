package com.example.demo.schoolclass;

import com.example.demo.school.SchoolProfile;
import com.example.demo.staff.StaffMember;
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
import java.util.UUID;

@Entity
@Table(
		name = "school_classes",
		indexes = {
				@Index(name = "idx_classes_school", columnList = "school_profile_id"),
				@Index(name = "idx_classes_unique_section", columnList = "school_profile_id,className,sectionName,academicYear", unique = true)
		}
)
public class SchoolClass {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_profile_id", nullable = false)
	private SchoolProfile schoolProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_teacher_id")
	private StaffMember classTeacher;

	@Column(nullable = false, length = 80)
	private String className;

	@Column(nullable = false, length = 80)
	private String sectionName;

	@Column(nullable = false, length = 40)
	private String academicYear;

	@Column(nullable = false)
	private int capacity;

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
	public StaffMember getClassTeacher() { return classTeacher; }
	public void setClassTeacher(StaffMember classTeacher) { this.classTeacher = classTeacher; }
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = clean(className); }
	public String getSectionName() { return sectionName; }
	public void setSectionName(String sectionName) { this.sectionName = clean(sectionName); }
	public String getAcademicYear() { return academicYear; }
	public void setAcademicYear(String academicYear) { this.academicYear = clean(academicYear); }
	public int getCapacity() { return capacity; }
	public void setCapacity(int capacity) { this.capacity = capacity; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = clean(status); }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	private void normalize() {
		className = clean(className);
		sectionName = clean(sectionName);
		academicYear = clean(academicYear);
		status = status == null || status.isBlank() ? "ACTIVE" : clean(status).toUpperCase();
	}

	private String clean(String value) { return value == null ? null : value.trim(); }
}