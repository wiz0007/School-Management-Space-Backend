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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceAccessServiceTests {
	private SchoolProfileRepository schoolProfileRepository;
	private SchoolClassRepository schoolClassRepository;
	private StaffRepository staffRepository;
	private StudentRepository studentRepository;
	private WorkspaceAccessService workspaceAccessService;

	@BeforeEach
	void setUp() {
		schoolProfileRepository = mock(SchoolProfileRepository.class);
		schoolClassRepository = mock(SchoolClassRepository.class);
		staffRepository = mock(StaffRepository.class);
		studentRepository = mock(StudentRepository.class);
		workspaceAccessService = new WorkspaceAccessService(
				schoolProfileRepository,
				schoolClassRepository,
				staffRepository,
				studentRepository
		);
	}

	@Test
	void requireSchoolProfileReturnsOnlyCurrentUsersProfile() {
		UserAccount owner = new UserAccount();
		SchoolProfile profile = new SchoolProfile();
		profile.setOwner(owner);
		when(schoolProfileRepository.findByOwner(owner)).thenReturn(Optional.of(profile));

		SchoolProfile result = workspaceAccessService.requireSchoolProfile(owner, "Missing profile");

		assertSame(profile, result);
	}

	@Test
	void requireSchoolProfileFailsWhenCurrentUserHasNoWorkspace() {
		UserAccount owner = new UserAccount();
		when(schoolProfileRepository.findByOwner(owner)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> workspaceAccessService.requireSchoolProfile(owner, "Missing profile")
		);

		assertEquals(HttpStatus.PRECONDITION_REQUIRED, exception.getStatusCode());
	}

	@Test
	void requireClassRejectsClassOutsideCurrentWorkspace() {
		SchoolProfile profile = new SchoolProfile();
		UUID classId = UUID.randomUUID();
		when(schoolClassRepository.findByIdAndSchoolProfile(classId, profile)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> workspaceAccessService.requireClass(profile, classId, "Class not found")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	void requireStaffReturnsOnlyStaffInsideCurrentWorkspace() {
		SchoolProfile profile = new SchoolProfile();
		UUID staffId = UUID.randomUUID();
		StaffMember staff = new StaffMember();
		staff.setSchoolProfile(profile);
		when(staffRepository.findByIdAndSchoolProfile(staffId, profile)).thenReturn(Optional.of(staff));

		StaffMember result = workspaceAccessService.requireStaff(profile, staffId, "Staff not found");

		assertSame(staff, result);
	}

	@Test
	void requireStudentRejectsStudentOutsideCurrentWorkspace() {
		SchoolProfile profile = new SchoolProfile();
		UUID studentId = UUID.randomUUID();
		when(studentRepository.findByIdAndSchoolProfile(studentId, profile)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> workspaceAccessService.requireStudent(profile, studentId, "Student not found")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	void requireClassReturnsOnlyClassInsideCurrentWorkspace() {
		SchoolProfile profile = new SchoolProfile();
		UUID classId = UUID.randomUUID();
		SchoolClass schoolClass = new SchoolClass();
		schoolClass.setSchoolProfile(profile);
		when(schoolClassRepository.findByIdAndSchoolProfile(classId, profile)).thenReturn(Optional.of(schoolClass));

		SchoolClass result = workspaceAccessService.requireClass(profile, classId, "Class not found");

		assertSame(schoolClass, result);
	}
}
