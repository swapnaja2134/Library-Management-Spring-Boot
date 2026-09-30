package com.example.librarymgnt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librarymgnt.entity.LoginData;
import com.example.librarymgnt.entity.RegisterRequest;
import com.example.librarymgnt.entity.UserResponse;
import com.example.librarymgnt.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest user) {
		UserResponse response = userService.addNewUser(user);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<UserResponse> loginCheck(@Valid @RequestBody LoginData log) {
		UserResponse logUser = userService.loginCheck(log);
		return ResponseEntity.ok(logUser);
	}
}
