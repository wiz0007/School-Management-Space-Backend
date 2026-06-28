package com.example.demo.school;

import com.example.demo.school.dto.SchoolProfileRequest;
import com.example.demo.school.dto.SchoolProfileResponse;
import com.example.demo.user.UserAccount;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolProfileService {
	private final SchoolProfileRepository schoolProfileRepository;

	public SchoolProfileService(SchoolProfileRepository schoolProfileRepository) {
		this.schoolProfileRepository = schoolProfileRepository;
	}

	@Transactional(readOnly = true)
	public Optional<SchoolProfileResponse> currentProfile(UserAccount owner) {
		return schoolProfileRepository.findByOwner(owner).map(SchoolProfileResponse::from);
	}

	@Transactional
	public SchoolProfileResponse save(UserAccount owner, SchoolProfileRequest request) {
		SchoolProfile profile = schoolProfileRepository.findByOwner(owner).orElseGet(() -> {
			SchoolProfile created = new SchoolProfile();
			created.setOwner(owner);
			return created;
		});

		profile.setSchoolName(request.schoolName());
		profile.setAddress(request.address());
		profile.setContactEmail(request.contactEmail());
		profile.setPhone(request.phone());
		profile.setAcademicYear(request.academicYear());
		profile.setPrincipalName(request.principalName());
		profile.setTimezone(request.timezone());
		profile.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());

		return SchoolProfileResponse.from(schoolProfileRepository.save(profile));
	}
}