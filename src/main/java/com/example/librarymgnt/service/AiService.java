package com.example.librarymgnt.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.librarymgnt.entity.AiSearchResponse;
import com.example.librarymgnt.entity.Book;
import com.example.librarymgnt.repository.BookRepository;

@Service
public class AiService {

	@Autowired
	private BookRepository bookRepository;

	@Value("${ai.api.key:}")
	private String apiKey;

	@Value("${ai.model:}")
	private String model;

	private RestClient restClient = RestClient.create();

	// search books using AI, if AI fails then do the normal title search
	public AiSearchResponse searchBooks(String question) {
		try {
			List<Book> allBooks = bookRepository.findAll();
			String prompt = makePrompt(question, allBooks);
			String answer = askAi(prompt);
			List<Book> matched = pickBooksFromAnswer(answer, allBooks);
			return new AiSearchResponse(matched, true, "");
		} catch (Exception e) {
			// key missing, wrong model name, busy server or limit reached
			System.out.println("AI call failed: " + e.getMessage());
			List<Book> normal = bookRepository.findByTitleContainingIgnoreCase(question);
			return new AiSearchResponse(normal, false,
					"AI is not available right now, showing normal search results");
		}
	}

	// builds the text we send to the AI
	private String makePrompt(String question, List<Book> books) {
		StringBuilder sb = new StringBuilder();
		sb.append("You are helping with a college library search.\n");
		sb.append("Here is the list of books (id | title | author | category):\n");

		// only the first 300 books so the prompt does not get too big
		int limit = Math.min(books.size(), 300);
		for (int i = 0; i < limit; i++) {
			Book b = books.get(i);
			sb.append(b.getBook_id()).append(" | ").append(b.getTitle()).append(" | ")
					.append(b.getAuthor()).append(" | ").append(b.getCategory()).append("\n");
		}

		sb.append("\nThe student wants: ").append(question).append("\n");
		sb.append("Reply with only the ids of the best matching books, separated by commas, ");
		sb.append("best match first. Give at most 5 ids. If nothing matches reply with NONE.");
		return sb.toString();
	}

	// sends one question to the AI and gives back its text answer
	private String askAi(String prompt) {
		if (apiKey.isBlank() || model.isBlank()) {
			throw new RuntimeException("AI key or model is not set");
		}

		String url = "https://generativelanguage.googleapis.com/v1beta/interactions";

		Map<String, Object> body = Map.of("model", model, "input", prompt);

		Map<String, Object> response = restClient.post()
				.uri(url)
				.header("x-goog-api-key", apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {});

		return getTextFromResponse(response);
	}

	// the answer text is inside steps -> model_output -> content -> text
	@SuppressWarnings("unchecked")
	private String getTextFromResponse(Map<String, Object> response) {
		List<Object> steps = (List<Object>) response.get("steps");

		for (Object stepObject : steps) {
			Map<String, Object> step = (Map<String, Object>) stepObject;

			if ("model_output".equals(step.get("type"))) {
				List<Object> contents = (List<Object>) step.get("content");

				for (Object contentObject : contents) {
					Map<String, Object> content = (Map<String, Object>) contentObject;

					if ("text".equals(content.get("type"))) {
						return (String) content.get("text");
					}
				}
			}
		}

		throw new RuntimeException("No text found in AI response");
	}

	// turns the answer like "3, 1" into real books from our own list
	private List<Book> pickBooksFromAnswer(String answer, List<Book> allBooks) {
		List<Book> result = new ArrayList<>();

		String[] pieces = answer.split(",");
		for (String piece : pieces) {
			String text = piece.trim();

			// ignore anything that is not a plain number, like NONE
			if (!text.matches("\\d{1,9}")) {
				continue;
			}

			int id = Integer.parseInt(text);
			for (Book b : allBooks) {
				if (b.getBook_id() == id && !result.contains(b)) {
					result.add(b);
				}
			}
		}
		return result;
	}
}