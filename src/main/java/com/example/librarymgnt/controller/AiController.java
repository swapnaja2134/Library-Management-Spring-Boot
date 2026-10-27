package com.example.librarymgnt.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librarymgnt.entity.AiSearchResponse;
import com.example.librarymgnt.exception.BadRequestException;
import com.example.librarymgnt.service.AiService;

@RestController
@RequestMapping("/ai")
public class AiController {

	@Autowired
	private AiService aiService;

	@PostMapping("/search")
	public ResponseEntity<AiSearchResponse> search(@RequestBody Map<String, String> request) {
		String question = request.get("question");

		if (question == null || question.isBlank()) {
			throw new BadRequestException("Please type what you are looking for");
		}
		if (question.length() > 200) {
			throw new BadRequestException("Question is too long, keep it under 200 characters");
		}

		return ResponseEntity.ok(aiService.searchBooks(question.trim()));
	}
}