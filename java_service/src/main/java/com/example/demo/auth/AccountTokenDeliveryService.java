package com.example.demo.auth;

import com.example.demo.config.SecurityProperties;
import com.example.demo.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountTokenDeliveryService {
	private static final Logger log = LoggerFactory.getLogger(AccountTokenDeliveryService.class);
	private final SecurityProperties securityProperties;

	public AccountTokenDeliveryService(SecurityProperties securityProperties) {
		this.securityProperties = securityProperties;
	}

	public void deliver(UserAccount user, AccountTokenPurpose purpose, String rawToken) {
		if (!securityProperties.accountTokenDevDeliveryEnabled()) {
			return;
		}

		String path = purpose == AccountTokenPurpose.EMAIL_VERIFICATION
				? "/verify-email?token="
				: "/reset-password?token=";
		log.warn(
				"development account-token delivery enabled email={} purpose={} link={}{}{}",
				user.getEmail(),
				purpose,
				securityProperties.publicFrontendUrl(),
				path,
				rawToken
		);
	}
}
