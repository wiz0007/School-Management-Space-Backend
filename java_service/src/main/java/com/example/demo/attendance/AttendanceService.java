package com.example.demo.attendance;

import com.example.demo.attendance.dto.AttendanceEntryRequest;
import com.example.demo.attendance.dto.AttendanceRecordResponse;
import com.example.demo.attendance.dto.AttendanceRosterStudentResponse;
import com.example.demo.attendance.dto.AttendanceSaveRequest;
import com.example.demo.school.SchoolProfile;
import com.example.demo.school.SchoolProfileRepository;
import com.example.demo.schoolclass.SchoolClass;
import com.example.demo.schoolclass.SchoolClassRepository;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import com.example.demo.user.UserAccount;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttendanceService {
	private final AttendanceRepository attendanceRepository;
	private final SchoolProfileRepository schoolProfileRepository;
	private final SchoolClassRepository schoolClassRepository;
	private final StudentRepository studentRepository;

	public AttendanceService(
			AttendanceRepository attendanceRepository,
			SchoolProfileRepository schoolProfileRepository,
			SchoolClassRepository schoolClassRepository,
			StudentRepository studentRepository
	) {
		this.attendanceRepository = attendanceRepository;
		this.schoolProfileRepository = schoolProfileRepository;
		this.schoolClassRepository = schoolClassRepository;
		this.studentRepository = studentRepository;
	}

	@Transactional(readOnly = true)
	public List<AttendanceRosterStudentResponse> roster(UserAccount owner, UUID classId, LocalDate attendanceDate) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		SchoolClass schoolClass = classFor(schoolProfile, classId);
		List<Student> students = studentRepository.findBySchoolProfileAndAssignedClassOrderByFullNameAsc(schoolProfile, schoolClass);
		Map<UUID, AttendanceRecord> existingRecords = attendanceRepository
				.findBySchoolProfileAndSchoolClassAndAttendanceDateOrderByStudentFullNameAsc(schoolProfile, schoolClass, attendanceDate)
				.stream()
				.collect(Collectors.toMap(record -> record.getStudent().getId(), Function.identity()));

		return students.stream()
				.map(student -> {
					AttendanceRecord existing = existingRecords.get(student.getId());
					return existing == null
							? AttendanceRosterStudentResponse.empty(student)
							: new AttendanceRosterStudentResponse(
									student.getId(),
									student.getFullName(),
									student.getAdmissionNumber(),
									existing.getStatus(),
									existing.getRemarks()
							);
				})
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceRecordResponse> records(UserAccount owner, UUID classId, LocalDate attendanceDate) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		SchoolClass schoolClass = classFor(schoolProfile, classId);
		return attendanceRepository
				.findBySchoolProfileAndSchoolClassAndAttendanceDateOrderByStudentFullNameAsc(schoolProfile, schoolClass, attendanceDate)
				.stream()
				.map(AttendanceRecordResponse::from)
				.toList();
	}

	@Transactional
	public List<AttendanceRecordResponse> save(UserAccount owner, AttendanceSaveRequest request) {
		SchoolProfile schoolProfile = schoolProfileFor(owner);
		SchoolClass schoolClass = classFor(schoolProfile, request.classId());

		for (AttendanceEntryRequest entry : request.entries()) {
			Student student = studentRepository.findByIdAndSchoolProfile(entry.studentId(), schoolProfile)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student must belong to this school."));
			if (student.getAssignedClass() == null || !student.getAssignedClass().getId().equals(schoolClass.getId())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student must be assigned to the selected class.");
			}

			AttendanceRecord record = attendanceRepository
					.findBySchoolProfileAndStudentAndAttendanceDate(schoolProfile, student, request.attendanceDate())
					.orElseGet(AttendanceRecord::new);
			record.setSchoolProfile(schoolProfile);
			record.setSchoolClass(schoolClass);
			record.setStudent(student);
			record.setAttendanceDate(request.attendanceDate());
			record.setStatus(entry.status());
			record.setRemarks(entry.remarks());
			attendanceRepository.save(record);
		}

		return records(owner, request.classId(), request.attendanceDate());
	}

	private SchoolProfile schoolProfileFor(UserAccount owner) {
		return schoolProfileRepository.findByOwner(owner)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "Create the school profile before marking attendance."));
	}

	private SchoolClass classFor(SchoolProfile schoolProfile, UUID classId) {
		return schoolClassRepository.findByIdAndSchoolProfile(classId, schoolProfile)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class must belong to this school."));
	}
}