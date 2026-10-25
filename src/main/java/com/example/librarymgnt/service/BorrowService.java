package com.example.librarymgnt.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.librarymgnt.entity.Book;
import com.example.librarymgnt.entity.Borrow;
import com.example.librarymgnt.entity.BorrowStatus;
import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;
import com.example.librarymgnt.exception.AccountDisabledException;
import com.example.librarymgnt.exception.BadRequestException;
import com.example.librarymgnt.exception.ResourceNotFoundException;
import com.example.librarymgnt.exception.UnauthorizedAccessException;
import com.example.librarymgnt.repository.BookRepository;
import com.example.librarymgnt.repository.BorrowRepository;
import com.example.librarymgnt.repository.UserRepository;

@Service
public class BorrowService {
	
	@Autowired
	private BorrowRepository borrowRepository;
	
	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private UserRepository userRepository;

	@Transactional
	public String borrowBook(Integer bookId) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new ResourceNotFoundException("Authenticated user not found");
		}

		if (Boolean.FALSE.equals(user.getEnabled())) {
			throw new AccountDisabledException("User account is disabled. Cannot borrow books.");
		}

		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

		if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
			throw new BadRequestException("Book is not available for borrowing");
		}

		if (borrowRepository.existsActiveBorrow(user.getUserid(), bookId, BorrowStatus.BORROWED)) {
			throw new BadRequestException("You have already borrowed this book");
		}
		
		book.setAvailableCopies(book.getAvailableCopies() - 1);
		bookRepository.save(book);

		Borrow borrow = new Borrow();
		borrow.setUser(user);
		borrow.setBook(book);
		borrow.setBorrowDate(LocalDate.now());
		borrow.setDueDate(LocalDate.now().plusDays(14));
		borrow.setStatus(BorrowStatus.BORROWED);

		borrowRepository.save(borrow);

		return "Book borrowed successfully";
	}

	@Transactional
	public String returnBook(Integer borrowId) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User currentUser = userRepository.findByEmail(email);

		if (currentUser == null) {
			throw new ResourceNotFoundException("Authenticated user not found");
		}

		Borrow borrow = borrowRepository.findById(borrowId)
				.orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + borrowId));

		// Authorization verification: Members can only return their own borrow records
		if (currentUser.getRole() == Role.MEMBER) {
			if (borrow.getUser() == null || !borrow.getUser().getUserid().equals(currentUser.getUserid())) {
				throw new UnauthorizedAccessException("Unauthorized: You can only return books borrowed by your own account");
			}
		}

		if (borrow.getStatus() == BorrowStatus.RETURNED) {
			throw new BadRequestException("Book already returned");
		}

		borrow.setReturnDate(LocalDate.now());
		borrow.setStatus(BorrowStatus.RETURNED);

		Book book = borrow.getBook();
		if (book != null) {
			book.setAvailableCopies(book.getAvailableCopies() + 1);
			bookRepository.save(book);
		}

		borrowRepository.save(borrow);

		return "Book returned successfully";
	}

	public List<Borrow> myBorrowHistory() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new ResourceNotFoundException("Authenticated user not found");
		}

		return borrowRepository.findByUserUserid(user.getUserid());
	}

	public List<Borrow> getAllBorrowRecords() {
		return borrowRepository.findAll();
	}
}
