package com.example.demo.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
	private static final Logger log = LoggerFactory.getLogger(AuditService.class);

	public void publish(AuditEvent event) {
		log.info(
				"audit actor={} action={} resource={} requestId={}",
				event.actor(),
				event.action(),
				event.resource(),
				event.requestId()
		);
	}
}
