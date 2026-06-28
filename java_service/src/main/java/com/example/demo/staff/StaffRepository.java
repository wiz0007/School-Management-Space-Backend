package com.example.demo.staff;

import com.example.demo.school.SchoolProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<StaffMember, UUID> {
	List<StaffMember> findBySchoolProfileOrderByCreatedAtDesc(SchoolProfile schoolProfile);
	Optional<StaffMember> findBySchoolProfileAndEmployeeCode(SchoolProfile schoolProfile, String employeeCode);
	Optional<StaffMember> findByIdAndSchoolProfile(UUID id, SchoolProfile schoolProfile);
}