package com.example.librarymgnt.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librarymgnt.entity.Borrow;
import com.example.librarymgnt.service.BorrowService;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

	@Autowired
	private BorrowService borrowService;

	@PostMapping("/{bookId}")
	public ResponseEntity<Map<String, String>> borrowBook(@PathVariable Integer bookId) {
		String result = borrowService.borrowBook(bookId);
		return ResponseEntity.ok(Collections.singletonMap("message", result));
	}

	@PutMapping("/return/{borrowId}")
	public ResponseEntity<Map<String, String>> returnBook(@PathVariable Integer borrowId) {
		String result = borrowService.returnBook(borrowId);
		return ResponseEntity.ok(Collections.singletonMap("message", result));
	}

	@GetMapping("/my")
	public ResponseEntity<List<Borrow>> myBorrowHistory() {
		return ResponseEntity.ok(borrowService.myBorrowHistory());
	}

	@GetMapping("/all")
	public ResponseEntity<List<Borrow>> allBorrowRecords() {
		return ResponseEntity.ok(borrowService.getAllBorrowRecords());
	}
}