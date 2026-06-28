package com.example.demo.student;

import com.example.demo.school.SchoolProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {
	List<Student> findBySchoolProfileOrderByCreatedAtDesc(SchoolProfile schoolProfile);
	Optional<Student> findBySchoolProfileAndAdmissionNumber(SchoolProfile schoolProfile, String admissionNumber);
}