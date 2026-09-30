package com.example.librarymgnt.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.librarymgnt.entity.RegisterRequest;
import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;
import com.example.librarymgnt.entity.UserResponse;
import com.example.librarymgnt.exception.ResourceNotFoundException;
import com.example.librarymgnt.exception.UserAlreadyExistsException;
import com.example.librarymgnt.repository.UserRepository;

@Service
public class AdminService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public UserResponse addLibrarian(RegisterRequest reqUser) {
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
		user.setRole(Role.LIBRARIAN);
		user.setEnabled(true);
		user.setCreatedAt(LocalDateTime.now());
		
		User temp = userRepository.save(user);
		return new UserResponse(
				temp.getUserid(),
				temp.getName(),
				temp.getUsername(),
				temp.getEmail(),
				temp.getRole(),
				temp.getEnabled(),
				null
		);
	}
	
	public List<UserResponse> getAllUsers() {
		List<UserResponse> userLists = new ArrayList<>();
		List<User> allUsers = userRepository.findAll();
		for (User temp : allUsers) {
			userLists.add(new UserResponse(
					temp.getUserid(),
					temp.getName(),
					temp.getUsername(),
					temp.getEmail(),
					temp.getRole(),
					temp.getEnabled(),
					null
			));
		}
		return userLists;
	}
	
	public List<UserResponse> getMembers() {
		List<UserResponse> memberLists = new ArrayList<>();
		List<User> allUsers = userRepository.findByRole(Role.MEMBER);
		for (User temp : allUsers) {
			memberLists.add(new UserResponse(
					temp.getUserid(),
					temp.getName(),
					temp.getUsername(),
					temp.getEmail(),
					temp.getRole(),
					temp.getEnabled(),
					null
			));
		}
		return memberLists;
	}
	
	public List<UserResponse> getLibrarians() {
		List<UserResponse> librarianLists = new ArrayList<>();
		List<User> allUsers = userRepository.findByRole(Role.LIBRARIAN);
		for (User temp : allUsers) {
			librarianLists.add(new UserResponse(
					temp.getUserid(),
					temp.getName(),
					temp.getUsername(),
					temp.getEmail(),
					temp.getRole(),
					temp.getEnabled(),
					null
			));
		}
		return librarianLists;
	}
	
	public UserResponse getUserById(Integer id) {
		User temp = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
		return new UserResponse(
				temp.getUserid(),
				temp.getName(),
				temp.getUsername(),
				temp.getEmail(),
				temp.getRole(),
				temp.getEnabled(),
				null
		);
	}
	
	public void deleteUser(Integer id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
		userRepository.delete(user);
	}
	
	public void disableUser(Integer id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
		user.setEnabled(false);
		userRepository.save(user);
	}
	
	public void enableUser(Integer id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
		user.setEnabled(true);
		userRepository.save(user);
	}
}
