package com.example.librarymgnt.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
	// fine for every day a book is late (in rupees)
	private static final int FINE_PER_DAY = 2;
	
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
		// check if the book is being returned late
		long lateDays = 0;
		if (borrow.getDueDate() != null) {
			lateDays = ChronoUnit.DAYS.between(borrow.getDueDate(), LocalDate.now());
		}

		borrow.setReturnDate(LocalDate.now());
		borrow.setStatus(BorrowStatus.RETURNED);

		Book book = borrow.getBook();
		if (book != null) {
			book.setAvailableCopies(book.getAvailableCopies() + 1);
			bookRepository.save(book);
		}

		borrowRepository.save(borrow);

		if (lateDays > 0) {
			return "Book returned. Late fine: Rs. " + (lateDays * FINE_PER_DAY);
		}
		return "Book returned successfully";
	}

	public List<Borrow> myBorrowHistory() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new ResourceNotFoundException("Authenticated user not found");
		}

		List<Borrow> records = borrowRepository.findByUserUserid(user.getUserid());
		for (Borrow record : records) {
			addFine(record);
		}
		return records;
	}

	public List<Borrow> getAllBorrowRecords() {
		List<Borrow> records = borrowRepository.findAll();
		for (Borrow record : records) {
			addFine(record);
		}
		return records;
	}

	
	
	// works out how many days late and the fine for one borrow record
	private void addFine(Borrow borrow) {
		borrow.setDaysLate(0);
		borrow.setFine(0);
		borrow.setOverdue(false);

		if (borrow.getDueDate() == null) {
			return;
		}

		// if the book is returned use the return date, otherwise use today
		LocalDate endDate = LocalDate.now();
		if (borrow.getReturnDate() != null) {
			endDate = borrow.getReturnDate();
		}

		long late = ChronoUnit.DAYS.between(borrow.getDueDate(), endDate);

		if (late > 0) {
			borrow.setDaysLate((int) late);
			borrow.setFine((int) late * FINE_PER_DAY);

			// a book that is not returned yet and is late is overdue
			if (borrow.getStatus() == BorrowStatus.BORROWED) {
				borrow.setOverdue(true);
			}
		}
	}
}
