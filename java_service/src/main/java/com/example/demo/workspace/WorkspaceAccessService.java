package com.example.demo.workspace;

import com.example.demo.school.SchoolProfile;
import com.example.demo.school.SchoolProfileRepository;
import com.example.demo.schoolclass.SchoolClass;
import com.example.demo.schoolclass.SchoolClassRepository;
import com.example.demo.staff.StaffMember;
import com.example.demo.staff.StaffRepository;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import com.example.demo.user.UserAccount;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkspaceAccessService {
	private final SchoolProfileRepository schoolProfileRepository;
	private final SchoolClassRepository schoolClassRepository;
	private final StaffRepository staffRepository;
	private final StudentRepository studentRepository;

	public WorkspaceAccessService(
			SchoolProfileRepository schoolProfileRepository,
			SchoolClassRepository schoolClassRepository,
			StaffRepository staffRepository,
			StudentRepository studentRepository
	) {
		this.schoolProfileRepository = schoolProfileRepository;
		this.schoolClassRepository = schoolClassRepository;
		this.staffRepository = staffRepository;
		this.studentRepository = studentRepository;
	}

	public SchoolProfile requireSchoolProfile(UserAccount owner, String missingMessage) {
		return schoolProfileRepository.findByOwner(owner)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, missingMessage));
	}

	public SchoolClass requireClass(SchoolProfile schoolProfile, UUID classId, String message) {
		return schoolClassRepository.findByIdAndSchoolProfile(classId, schoolProfile)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
	}

	public StaffMember requireStaff(SchoolProfile schoolProfile, UUID staffId, String message) {
		return staffRepository.findByIdAndSchoolProfile(staffId, schoolProfile)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
	}

	public Student requireStudent(SchoolProfile schoolProfile, UUID studentId, String message) {
		return studentRepository.findByIdAndSchoolProfile(studentId, schoolProfile)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
	}
}
