package com.example.demo;

import com.example.demo.auth.RefreshTokenRepository;
import com.example.demo.school.SchoolProfileRepository;
import com.example.demo.staff.StaffRepository;
import com.example.demo.student.StudentRepository;
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

		@Bean
		RefreshTokenRepository refreshTokenRepository() {
			return Mockito.mock(RefreshTokenRepository.class);
		}

		@Bean
		SchoolProfileRepository schoolProfileRepository() {
			return Mockito.mock(SchoolProfileRepository.class);
		}

		@Bean
		StudentRepository studentRepository() {
			return Mockito.mock(StudentRepository.class);
		}

		@Bean
		StaffRepository staffRepository() {
			return Mockito.mock(StaffRepository.class);
		}
	}
}