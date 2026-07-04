package com.example.demo.schoolclass;

import com.example.demo.school.SchoolProfile;
import com.example.demo.schoolclass.dto.SchoolClassRequest;
import com.example.demo.schoolclass.dto.SchoolClassResponse;
import com.example.demo.staff.StaffMember;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.workspace.WorkspaceAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SchoolClassService {
	private final SchoolClassRepository schoolClassRepository;
	private final StaffRepository staffRepository;
	private final WorkspaceAccessService workspaceAccessService;

	public SchoolClassService(
			SchoolClassRepository schoolClassRepository,
			StaffRepository staffRepository,
			WorkspaceAccessService workspaceAccessService
	) {
		this.schoolClassRepository = schoolClassRepository;
		this.staffRepository = staffRepository;
		this.workspaceAccessService = workspaceAccessService;
	}

	@Transactional(readOnly = true)
	public List<SchoolClassResponse> list(UserAccount owner) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		return schoolClassRepository.findBySchoolProfileOrderByCreatedAtDesc(schoolProfile).stream()
				.map(SchoolClassResponse::from)
				.toList();
	}

	@Transactional
	public SchoolClassResponse create(UserAccount owner, SchoolClassRequest request) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		ensureUniqueSection(schoolProfile, request, null);

		SchoolClass schoolClass = new SchoolClass();
		schoolClass.setSchoolProfile(schoolProfile);
		apply(schoolClass, schoolProfile, request);
		return SchoolClassResponse.from(schoolClassRepository.save(schoolClass));
	}

	@Transactional
	public SchoolClassResponse update(UserAccount owner, UUID id, SchoolClassRequest request) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		SchoolClass schoolClass = workspaceAccessService.requireClass(schoolProfile, id, "Class record not found.");
		ensureUniqueSection(schoolProfile, request, schoolClass.getId());
		apply(schoolClass, schoolProfile, request);
		return SchoolClassResponse.from(schoolClassRepository.save(schoolClass));
	}

	private SchoolProfile schoolProfileFor(UserAccount owner) {
		return workspaceAccessService.requireSchoolProfile(owner, "Create the school profile before adding classes.");
	}

	private void ensureUniqueSection(SchoolProfile schoolProfile, SchoolClassRequest request, UUID currentId) {
		schoolClassRepository.findBySchoolProfileAndClassNameAndSectionNameAndAcademicYear(
				schoolProfile,
				clean(request.className()),
				clean(request.sectionName()),
				clean(request.academicYear())
		)
				.filter(existing -> currentId == null || !existing.getId().equals(currentId))
				.ifPresent(existing -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Class section already exists for this academic year.");
				});
	}

	private void apply(SchoolClass schoolClass, SchoolProfile schoolProfile, SchoolClassRequest request) {
		schoolClass.setClassName(request.className());
		schoolClass.setSectionName(request.sectionName());
		schoolClass.setAcademicYear(request.academicYear());
		schoolClass.setCapacity(request.capacity());
		schoolClass.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());
		schoolClass.setClassTeacher(resolveClassTeacher(schoolProfile, request.classTeacherId()));
	}

	private StaffMember resolveClassTeacher(SchoolProfile schoolProfile, UUID classTeacherId) {
		if (classTeacherId == null) {
			return null;
		}
		return staffRepository.findByIdAndSchoolProfile(classTeacherId, schoolProfile)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class teacher must belong to this school."));
	}

	private String clean(String value) {
		return value == null ? null : value.trim();
	}
}
