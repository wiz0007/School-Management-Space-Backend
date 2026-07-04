package com.example.demo.auth;

import com.example.demo.user.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTokenRepository extends JpaRepository<AccountToken, UUID> {
	Optional<AccountToken> findByTokenHashAndPurpose(String tokenHash, AccountTokenPurpose purpose);

	List<AccountToken> findByUserAndPurposeAndConsumedAtIsNull(UserAccount user, AccountTokenPurpose purpose);
}
