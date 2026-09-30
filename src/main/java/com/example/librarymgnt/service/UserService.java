package com.example.librarymgnt.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.librarymgnt.entity.LoginData;
import com.example.librarymgnt.entity.RegisterRequest;
import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;
import com.example.librarymgnt.entity.UserResponse;
import com.example.librarymgnt.exception.AccountDisabledException;
import com.example.librarymgnt.exception.UserAlreadyExistsException;
import com.example.librarymgnt.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;
	
	public UserResponse addNewUser(RegisterRequest reqUser) {
		if (userRepository.existsByUsername(reqUser.getUsername())) {
			throw new UserAlreadyExistsException("Username already exists: " + reqUser.getUsername());
		}
		
		if (userRepository.existsByEmail(reqUser.getEmail())) {
			throw new UserAlreadyExistsException("Email already exists: " + reqUser.getEmail());
		}

		User user = new User();
		user.setName(reqUser.getName());
		user.setUsername(reqUser.getUsername());
		user.setEmail(reqUser.getEmail());
		user.setPassword(passwordEncoder.encode(reqUser.getPassword()));
		user.setPhone(reqUser.getPhone());
		user.setAddress(reqUser.getAddress());
		user.setRole(Role.MEMBER);
		user.setEnabled(true);
		user.setCreatedAt(LocalDateTime.now());
		
		User savedUser = userRepository.save(user);
		return new UserResponse(
				savedUser.getUserid(),
				savedUser.getName(),
				savedUser.getUsername(),
				savedUser.getEmail(),
				savedUser.getRole(),
				savedUser.getEnabled(),
				null
		);
	}
	
	public UserResponse loginCheck(LoginData login) {
		User user = userRepository.findByUsername(login.getUsername());
		if (user == null) {
			throw new BadCredentialsException("Invalid username or password");
		}

		if (Boolean.FALSE.equals(user.getEnabled())) {
			throw new AccountDisabledException("User account is disabled. Please contact the administrator.");
		}
		
		boolean isMatch = passwordEncoder.matches(login.getPassword(), user.getPassword());
		if (!isMatch) {
			throw new BadCredentialsException("Invalid username or password");
		}
		
		String token = jwtService.generateToken(user);
		return new UserResponse(
				user.getUserid(),
				user.getName(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				user.getEnabled(),
				token
		);
	}
}
