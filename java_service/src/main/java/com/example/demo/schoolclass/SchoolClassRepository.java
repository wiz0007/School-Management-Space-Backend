package com.example.demo.schoolclass;

import com.example.demo.school.SchoolProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
	List<SchoolClass> findBySchoolProfileOrderByCreatedAtDesc(SchoolProfile schoolProfile);
	Optional<SchoolClass> findByIdAndSchoolProfile(UUID id, SchoolProfile schoolProfile);
	Optional<SchoolClass> findBySchoolProfileAndClassNameAndSectionNameAndAcademicYear(
			SchoolProfile schoolProfile,
			String className,
			String sectionName,
			String academicYear
	);
}