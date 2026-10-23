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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.librarymgnt.entity.Book;
import com.example.librarymgnt.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/book")
public class BookController {

	@Autowired
	private BookService bookService;
	
    @PostMapping("/add")
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
    	Book createdBook = bookService.addBook(book);
    	return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Integer id, @Valid @RequestBody Book book) {
    	Book updatedBook = bookService.updateBook(id, book);
    	return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Integer id) {
    	bookService.deleteBook(id);
    	return ResponseEntity.ok(Collections.singletonMap("message", "Book deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Integer id) {
    	Book book = bookService.getBook(id);
    	return ResponseEntity.ok(book);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooks() {
    	return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> availableBooks() {
    	return ResponseEntity.ok(bookService.availableBooks());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String title) {
        return ResponseEntity.ok(bookService.searchBooks(title));
    }
}