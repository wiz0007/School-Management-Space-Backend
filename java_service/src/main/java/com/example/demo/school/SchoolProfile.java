package com.example.demo.school;

import com.example.demo.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "school_profiles")
public class SchoolProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false, unique = true)
	private UserAccount owner;

	@Column(nullable = false, length = 160)
	private String schoolName;

	@Column(nullable = false, length = 260)
	private String address;

	@Column(nullable = false, length = 180)
	private String contactEmail;

	@Column(nullable = false, length = 40)
	private String phone;

	@Column(nullable = false, length = 40)
	private String academicYear;

	@Column(nullable = false, length = 120)
	private String principalName;

	@Column(nullable = false, length = 80)
	private String timezone;

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

	public UUID getId() {
		return id;
	}

	public UserAccount getOwner() {
		return owner;
	}

	public void setOwner(UserAccount owner) {
		this.owner = owner;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = clean(schoolName);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = clean(address);
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = normalizeEmail(contactEmail);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = clean(phone);
	}

	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = clean(academicYear);
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = clean(principalName);
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = clean(timezone);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = clean(status);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	private void normalize() {
		schoolName = clean(schoolName);
		address = clean(address);
		contactEmail = normalizeEmail(contactEmail);
		phone = clean(phone);
		academicYear = clean(academicYear);
		principalName = clean(principalName);
		timezone = clean(timezone);
		status = status == null || status.isBlank() ? "ACTIVE" : clean(status).toUpperCase();
	}

	private String clean(String value) {
		return value == null ? null : value.trim();
	}

	private String normalizeEmail(String value) {
		return value == null ? null : value.trim().toLowerCase();
	}
}