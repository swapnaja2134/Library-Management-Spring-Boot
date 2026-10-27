package com.example.librarymgnt.entity;

import java.util.List;

public class AiSearchResponse {

	private List<Book> books;
	private boolean usedAi;
	private String note;

	public AiSearchResponse(List<Book> books, boolean usedAi, String note) {
		super();
		this.books = books;
		this.usedAi = usedAi;
		this.note = note;
	}

	public AiSearchResponse() {
		super();
	}

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}

	public boolean isUsedAi() {
		return usedAi;
	}

	public void setUsedAi(boolean usedAi) {
		this.usedAi = usedAi;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}