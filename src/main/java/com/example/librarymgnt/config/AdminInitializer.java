package com.example.librarymgnt.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;
import com.example.librarymgnt.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.username:admin}")
	private String adminUsername;

	@Value("${app.admin.password:Admin@123}")
	private String adminPassword;

	@Value("${app.admin.email:admin@library.com}")
	private String adminEmail;

	@Value("${app.admin.name:System Administrator}")
	private String adminName;

	public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		if (!userRepository.existsByUsername(adminUsername) && userRepository.findByEmail(adminEmail) == null) {
			User admin = new User();
			admin.setName(adminName);
			admin.setUsername(adminUsername);
			admin.setEmail(adminEmail);
			admin.setPassword(passwordEncoder.encode(adminPassword));
			admin.setPhone("1234567890");
			admin.setAddress("Headquarters");
			admin.setRole(Role.ADMIN);
			admin.setEnabled(true);

			userRepository.save(admin);
			logger.info("Default ADMIN user [{}] created successfully from configuration", adminUsername);
		}
	}
}
