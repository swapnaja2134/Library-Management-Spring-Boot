package com.example.librarymgnt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.librarymgnt.entity.Book;
import com.example.librarymgnt.exception.BadRequestException;
import com.example.librarymgnt.exception.ResourceNotFoundException;
import com.example.librarymgnt.repository.BookRepository;

@Service
public class BookService {

	@Autowired
	private BookRepository bookRepository;
	
	public Book addBook(Book book) {
		if (book.getAvailableCopies() > book.getTotalCopies()) {
			throw new BadRequestException("Available copies cannot be greater than total copies.");
		}
		return bookRepository.save(book);
	}

	public Book updateBook(Integer id, Book updatedBook) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

		if (updatedBook.getAvailableCopies() > updatedBook.getTotalCopies()) {
			throw new BadRequestException("Available copies cannot be greater than total copies.");
		}

		book.setTitle(updatedBook.getTitle());
		book.setAuthor(updatedBook.getAuthor());
		book.setIsbn(updatedBook.getIsbn());
		book.setCategory(updatedBook.getCategory());
		book.setTotalCopies(updatedBook.getTotalCopies());
		book.setAvailableCopies(updatedBook.getAvailableCopies());

		return bookRepository.save(book);
	}

	public void deleteBook(Integer id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		bookRepository.delete(book);
	}

	public Book getBook(Integer id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
	}

	public List<Book> getAllBooks() {
		return bookRepository.findAll();
	}

	public List<Book> availableBooks() {
		return bookRepository.findByAvailableCopiesGreaterThan(0);
	}
}
