package com.example.librarymgnt.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="book")
public class Book {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private Integer book_id;
	
	@NotBlank(message = "Title is required")
	private String title;

	@NotBlank(message = "Author is required")
	private String author;

	@NotBlank(message = "ISBN is required")
	private String isbn;

	@NotBlank(message = "Category is required")
	private String category;

	@NotNull(message = "Total copies cannot be null")
	@Min(value = 0, message = "Total copies cannot be negative")
	private Integer totalCopies;
	
	@NotNull(message = "Available copies cannot be null")
	@Min(value = 0, message = "Available copies cannot be negative")
	private Integer availableCopies;
	
	@OneToMany(
	        mappedBy="book",
	        cascade=CascadeType.ALL
	)
	@JsonIgnore
	private List<Borrow> borrowRecords;

	public Book(String title, String author, String isbn, String category, Integer totalCopies,
			Integer availableCopies) {
		super();
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.category = category;
		this.totalCopies = totalCopies;
		this.availableCopies = availableCopies;
	}

	public Book() {
		super();
	}

	public Integer getBook_id() {
		return book_id;
	}

	public void setBook_id(Integer book_id) {
		this.book_id = book_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getTotalCopies() {
		return totalCopies;
	}

	public void setTotalCopies(Integer totalCopies) {
		this.totalCopies = totalCopies;
	}

	public Integer getAvailableCopies() {
		return availableCopies;
	}

	public void setAvailableCopies(Integer availableCopies) {
		this.availableCopies = availableCopies;
	}

	public List<Borrow> getBorrowRecords() {
		return borrowRecords;
	}

	public void setBorrowRecords(List<Borrow> borrowRecords) {
		this.borrowRecords = borrowRecords;
	}
}
