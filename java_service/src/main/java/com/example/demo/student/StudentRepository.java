package com.example.demo.student;

import com.example.demo.school.SchoolProfile;
import com.example.demo.schoolclass.SchoolClass;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {
	List<Student> findBySchoolProfileOrderByCreatedAtDesc(SchoolProfile schoolProfile);
	List<Student> findBySchoolProfileAndAssignedClassOrderByFullNameAsc(SchoolProfile schoolProfile, SchoolClass assignedClass);
	Optional<Student> findBySchoolProfileAndAdmissionNumber(SchoolProfile schoolProfile, String admissionNumber);
	Optional<Student> findByIdAndSchoolProfile(UUID id, SchoolProfile schoolProfile);
}