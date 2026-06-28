package com.example.demo.staff;

import com.example.demo.school.SchoolProfile;
import com.example.demo.school.SchoolProfileRepository;
import com.example.demo.staff.dto.StaffRequest;
import com.example.demo.staff.dto.StaffResponse;
import com.example.demo.user.UserAccount;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffService {
	private final StaffRepository staffRepository;
	private final SchoolProfileRepository schoolProfileRepository;

	public StaffService(StaffRepository staffRepository, SchoolProfileRepository schoolProfileRepository) {
		this.staffRepository = staffRepository;
		this.schoolProfileRepository = schoolProfileRepository;
	}

	@Transactional(readOnly = true)
	public List<StaffResponse> list(UserAccount owner) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		return staffRepository.findBySchoolProfileOrderByCreatedAtDesc(schoolProfile).stream()
				.map(StaffResponse::from)
				.toList();
	}

	@Transactional
	public StaffResponse create(UserAccount owner, StaffRequest request) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		ensureUniqueEmployeeCode(schoolProfile, request.employeeCode(), null);

		StaffMember staff = new StaffMember();
		staff.setSchoolProfile(schoolProfile);
		apply(staff, request);
		return StaffResponse.from(staffRepository.save(staff));
	}

	@Transactional
	public StaffResponse update(UserAccount owner, UUID id, StaffRequest request) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		StaffMember staff = staffRepository.findByIdAndSchoolProfile(id, schoolProfile)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff record not found."));
		ensureUniqueEmployeeCode(schoolProfile, request.employeeCode(), staff.getId());
		apply(staff, request);
		return StaffResponse.from(staffRepository.save(staff));
	}

	private SchoolProfile schoolProfileFor(UserAccount owner) {
		return schoolProfileRepository.findByOwner(owner)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "Create the school profile before adding staff."));
	}

	private void ensureUniqueEmployeeCode(SchoolProfile schoolProfile, String employeeCode, UUID currentId) {
		String normalizedCode = employeeCode == null ? null : employeeCode.trim();
		staffRepository.findBySchoolProfileAndEmployeeCode(schoolProfile, normalizedCode)
				.filter(existing -> currentId == null || !existing.getId().equals(currentId))
				.ifPresent(existing -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists for this school.");
				});
	}

	private void apply(StaffMember staff, StaffRequest request) {
		staff.setFullName(request.fullName());
		staff.setEmployeeCode(request.employeeCode());
		staff.setRole(request.role());
		staff.setEmail(request.email());
		staff.setPhone(request.phone());
		staff.setDepartment(request.department());
		staff.setJoiningDate(request.joiningDate());
		staff.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());
	}
}