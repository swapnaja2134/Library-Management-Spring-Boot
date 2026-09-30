package com.example.librarymgnt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.librarymgnt.entity.Book;
import com.example.librarymgnt.entity.Borrow;
import com.example.librarymgnt.entity.BorrowStatus;
import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;
import com.example.librarymgnt.exception.BadRequestException;
import com.example.librarymgnt.exception.UnauthorizedAccessException;
import com.example.librarymgnt.repository.BookRepository;
import com.example.librarymgnt.repository.BorrowRepository;
import com.example.librarymgnt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowService borrowService;

    private User member1;
    private User member2;
    private Book testBook;

    @BeforeEach
    void setUp() {
        member1 = new User();
        member1.setUserid(1);
        member1.setUsername("member1");
        member1.setEmail("member1@example.com");
        member1.setRole(Role.MEMBER);
        member1.setEnabled(true);

        member2 = new User();
        member2.setUserid(2);
        member2.setUsername("member2");
        member2.setEmail("member2@example.com");
        member2.setRole(Role.MEMBER);
        member2.setEnabled(true);

        testBook = new Book("Effective Java", "Joshua Bloch", "978-0134685991", "Programming", 5, 5);
        testBook.setBook_id(10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void borrowBook_Success_DecrementsAvailableCopies() {
        authenticateAs("member1@example.com");
        when(userRepository.findByEmail("member1@example.com")).thenReturn(member1);
        when(bookRepository.findById(10)).thenReturn(Optional.of(testBook));

        String result = borrowService.borrowBook(10);

        assertEquals("Book borrowed successfully", result);
        assertEquals(4, testBook.getAvailableCopies());
        verify(bookRepository).save(testBook);
        verify(borrowRepository).save(any(Borrow.class));
    }

    @Test
    void borrowBook_ThrowsBadRequest_WhenNoCopiesAvailable() {
        authenticateAs("member1@example.com");
        testBook.setAvailableCopies(0);
        when(userRepository.findByEmail("member1@example.com")).thenReturn(member1);
        when(bookRepository.findById(10)).thenReturn(Optional.of(testBook));

        assertThrows(BadRequestException.class, () -> borrowService.borrowBook(10));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    @Test
    void returnBook_Success_WhenMemberReturnsOwnBorrowedBook() {
        authenticateAs("member1@example.com");
        when(userRepository.findByEmail("member1@example.com")).thenReturn(member1);

        Borrow borrow = new Borrow();
        borrow.setId(100);
        borrow.setUser(member1);
        borrow.setBook(testBook);
        borrow.setStatus(BorrowStatus.BORROWED);
        borrow.setBorrowDate(LocalDate.now().minusDays(3));

        testBook.setAvailableCopies(4);
        when(borrowRepository.findById(100)).thenReturn(Optional.of(borrow));

        String result = borrowService.returnBook(100);

        assertEquals("Book returned successfully", result);
        assertEquals(BorrowStatus.RETURNED, borrow.getStatus());
        assertNotNull(borrow.getReturnDate());
        assertEquals(5, testBook.getAvailableCopies());
        verify(bookRepository).save(testBook);
        verify(borrowRepository).save(borrow);
    }

    @Test
    void returnBook_ThrowsUnauthorizedAccessException_WhenMemberReturnsAnotherMembersBook() {
        // Logged in as member2
        authenticateAs("member2@example.com");
        when(userRepository.findByEmail("member2@example.com")).thenReturn(member2);

        // Borrow record belongs to member1
        Borrow borrow = new Borrow();
        borrow.setId(100);
        borrow.setUser(member1);
        borrow.setBook(testBook);
        borrow.setStatus(BorrowStatus.BORROWED);

        when(borrowRepository.findById(100)).thenReturn(Optional.of(borrow));

        assertThrows(UnauthorizedAccessException.class, () -> borrowService.returnBook(100));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    @Test
    void returnBook_ThrowsBadRequest_WhenBookAlreadyReturned() {
        authenticateAs("member1@example.com");
        when(userRepository.findByEmail("member1@example.com")).thenReturn(member1);

        Borrow borrow = new Borrow();
        borrow.setId(100);
        borrow.setUser(member1);
        borrow.setBook(testBook);
        borrow.setStatus(BorrowStatus.RETURNED);

        when(borrowRepository.findById(100)).thenReturn(Optional.of(borrow));

        assertThrows(BadRequestException.class, () -> borrowService.returnBook(100));
    }
}
