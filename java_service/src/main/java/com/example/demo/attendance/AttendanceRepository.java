package com.example.demo.attendance;

import com.example.demo.school.SchoolProfile;
import com.example.demo.schoolclass.SchoolClass;
import com.example.demo.student.Student;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
	List<AttendanceRecord> findBySchoolProfileAndSchoolClassAndAttendanceDateOrderByStudentFullNameAsc(
			SchoolProfile schoolProfile,
			SchoolClass schoolClass,
			LocalDate attendanceDate
	);

	Optional<AttendanceRecord> findBySchoolProfileAndStudentAndAttendanceDate(
			SchoolProfile schoolProfile,
			Student student,
			LocalDate attendanceDate
	);
}