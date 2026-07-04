package com.example.demo;

import com.example.demo.attendance.AttendanceRepository;
import com.example.demo.audit.AuditLogRepository;
import com.example.demo.auth.AccountTokenRepository;
import com.example.demo.auth.LoginAttemptRepository;
import com.example.demo.auth.RefreshTokenRepository;
import com.example.demo.school.SchoolProfileRepository;
import com.example.demo.schoolclass.SchoolClassRepository;
import com.example.demo.staff.StaffRepository;
import com.example.demo.student.StudentRepository;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

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
		AccountTokenRepository accountTokenRepository() {
			return Mockito.mock(AccountTokenRepository.class);
		}

		@Bean
		LoginAttemptRepository loginAttemptRepository() {
			return Mockito.mock(LoginAttemptRepository.class);
		}

		@Bean
		AuditLogRepository auditLogRepository() {
			return Mockito.mock(AuditLogRepository.class);
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

		@Bean
		SchoolClassRepository schoolClassRepository() {
			return Mockito.mock(SchoolClassRepository.class);
		}

		@Bean
		AttendanceRepository attendanceRepository() {
			return Mockito.mock(AttendanceRepository.class);
		}
	}
}
