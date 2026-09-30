package com.example.librarymgnt.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librarymgnt.entity.RegisterRequest;
import com.example.librarymgnt.entity.UserResponse;
import com.example.librarymgnt.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@PostMapping("/librarian")
	public ResponseEntity<UserResponse> addLibrarian(@Valid @RequestBody RegisterRequest obj) {
		UserResponse response = adminService.addLibrarian(obj);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping("/users")
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(adminService.getAllUsers());
	}

	@GetMapping("/members")
	public ResponseEntity<List<UserResponse>> getMembers() {
		return ResponseEntity.ok(adminService.getMembers());
	}

	@GetMapping("/librarians")
	public ResponseEntity<List<UserResponse>> getLibrarians() {
		return ResponseEntity.ok(adminService.getLibrarians());
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<UserResponse> getUser(@PathVariable Integer id) {
		return ResponseEntity.ok(adminService.getUserById(id));
	}

	@DeleteMapping("/user/{id}")
	public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer id) {
		adminService.deleteUser(id);
		return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully"));
	}

	@PutMapping("/disable/{id}")
	public ResponseEntity<Map<String, String>> disableUser(@PathVariable Integer id) {
		adminService.disableUser(id);
		return ResponseEntity.ok(Collections.singletonMap("message", "User disabled successfully"));
	}

	@PutMapping("/enable/{id}")
	public ResponseEntity<Map<String, String>> enableUser(@PathVariable Integer id) {
		adminService.enableUser(id);
		return ResponseEntity.ok(Collections.singletonMap("message", "User enabled successfully"));
	}
}
