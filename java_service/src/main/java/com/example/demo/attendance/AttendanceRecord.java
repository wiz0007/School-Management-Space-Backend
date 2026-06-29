package com.example.demo.attendance;

import com.example.demo.school.SchoolProfile;
import com.example.demo.schoolclass.SchoolClass;
import com.example.demo.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
		name = "attendance_records",
		indexes = {
				@Index(name = "idx_attendance_school_date", columnList = "school_profile_id,attendanceDate"),
				@Index(name = "idx_attendance_class_date", columnList = "school_class_id,attendanceDate"),
				@Index(name = "idx_attendance_student_date", columnList = "student_id,attendanceDate", unique = true)
		}
)
public class AttendanceRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_profile_id", nullable = false)
	private SchoolProfile schoolProfile;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_class_id", nullable = false)
	private SchoolClass schoolClass;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@Column(nullable = false)
	private LocalDate attendanceDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AttendanceStatus status = AttendanceStatus.PRESENT;

	@Column(length = 240)
	private String remarks;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		remarks = clean(remarks);
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
		remarks = clean(remarks);
	}

	public UUID getId() { return id; }
	public SchoolProfile getSchoolProfile() { return schoolProfile; }
	public void setSchoolProfile(SchoolProfile schoolProfile) { this.schoolProfile = schoolProfile; }
	public SchoolClass getSchoolClass() { return schoolClass; }
	public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
	public Student getStudent() { return student; }
	public void setStudent(Student student) { this.student = student; }
	public LocalDate getAttendanceDate() { return attendanceDate; }
	public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
	public AttendanceStatus getStatus() { return status; }
	public void setStatus(AttendanceStatus status) { this.status = status; }
	public String getRemarks() { return remarks; }
	public void setRemarks(String remarks) { this.remarks = clean(remarks); }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}