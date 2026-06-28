package com.example.demo.student;

import com.example.demo.school.SchoolProfile;
import com.example.demo.school.SchoolProfileRepository;
import com.example.demo.student.dto.StudentRequest;
import com.example.demo.student.dto.StudentResponse;
import com.example.demo.user.UserAccount;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentService {
	private final StudentRepository studentRepository;
	private final SchoolProfileRepository schoolProfileRepository;

	public StudentService(StudentRepository studentRepository, SchoolProfileRepository schoolProfileRepository) {
		this.studentRepository = studentRepository;
		this.schoolProfileRepository = schoolProfileRepository;
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> list(UserAccount owner) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		return studentRepository.findBySchoolProfileOrderByCreatedAtDesc(schoolProfile).stream()
				.map(StudentResponse::from)
				.toList();
	}

	@Transactional
	public StudentResponse create(UserAccount owner, StudentRequest request) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		String admissionNumber = request.admissionNumber() == null ? null : request.admissionNumber().trim();
		studentRepository.findBySchoolProfileAndAdmissionNumber(schoolProfile, admissionNumber)
				.ifPresent(existing -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Admission number already exists for this school.");
				});

		Student student = new Student();
		student.setSchoolProfile(schoolProfile);
		apply(student, request);
		return StudentResponse.from(studentRepository.save(student));
	}

	private SchoolProfile schoolProfileFor(UserAccount owner) {
		return schoolProfileRepository.findByOwner(owner)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "Create the school profile before adding students."));
	}

	private void apply(Student student, StudentRequest request) {
		student.setFullName(request.fullName());
		student.setAdmissionNumber(request.admissionNumber());
		student.setClassName(request.className());
		student.setSectionName(request.sectionName());
		student.setDateOfBirth(request.dateOfBirth());
		student.setGender(request.gender());
		student.setGuardianName(request.guardianName());
		student.setGuardianPhone(request.guardianPhone());
		student.setGuardianEmail(request.guardianEmail());
		student.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());
	}
}