package com.example.demo.school;

import com.example.demo.user.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolProfileRepository extends JpaRepository<SchoolProfile, UUID> {
	Optional<SchoolProfile> findByOwner(UserAccount owner);
}