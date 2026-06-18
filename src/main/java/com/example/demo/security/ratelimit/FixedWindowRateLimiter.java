package com.example.demo.security.ratelimit;

import com.example.demo.config.SecurityProperties;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FixedWindowRateLimiter {
	private final Map<String, Window> windows = new ConcurrentHashMap<>();
	private final SecurityProperties properties;
	private final Clock clock;

	@Autowired
	public FixedWindowRateLimiter(SecurityProperties properties) {
		this(properties, Clock.systemUTC());
	}

	FixedWindowRateLimiter(SecurityProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	public boolean allow(String key) {
		long now = clock.millis();
		long windowMillis = properties.rateLimitWindow().toMillis();
		long currentWindow = now / windowMillis;

		Window window = windows.compute(key, (ignored, existing) -> {
			if (existing == null || existing.id != currentWindow) {
				return new Window(currentWindow, new AtomicInteger(1));
			}
			existing.count.incrementAndGet();
			return existing;
		});

		if (windows.size() > 10_000) {
			windows.entrySet().removeIf(entry -> entry.getValue().id < currentWindow - 2);
		}

		return window.count.get() <= properties.rateLimitCapacity();
	}

	private record Window(long id, AtomicInteger count) {
	}
}
