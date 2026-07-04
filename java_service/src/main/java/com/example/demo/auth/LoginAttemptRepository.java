package com.example.demo.auth;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
	long countByEmailHashAndSuccessfulFalseAndAttemptedAtAfter(String emailHash, Instant attemptedAt);

	long countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(String ipAddress, Instant attemptedAt);
}
