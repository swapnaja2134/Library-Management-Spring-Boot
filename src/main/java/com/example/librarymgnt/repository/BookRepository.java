package com.example.librarymgnt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librarymgnt.entity.Book;

public interface BookRepository extends JpaRepository<Book, Integer> {

	List<Book> findByTitleContainingIgnoreCase(
	        String title
	);
	List<Book> findByAvailableCopiesGreaterThan(int no);
}
