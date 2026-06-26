package com.example.demo;

import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestRepositories {
		@Bean
		UserRepository userRepository() {
			return Mockito.mock(UserRepository.class);
		}
	}
}
