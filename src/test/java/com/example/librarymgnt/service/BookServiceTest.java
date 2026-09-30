package com.example.librarymgnt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.librarymgnt.entity.Book;
import com.example.librarymgnt.exception.BadRequestException;
import com.example.librarymgnt.exception.ResourceNotFoundException;
import com.example.librarymgnt.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book validBook;

    @BeforeEach
    void setUp() {
        validBook = new Book("Clean Code", "Robert C. Martin", "978-0132350884", "Software", 10, 10);
    }

    @Test
    void addBook_Success_WhenCopiesAreValid() {
        when(bookRepository.save(validBook)).thenReturn(validBook);

        Book saved = bookService.addBook(validBook);

        assertNotNull(saved);
        assertEquals("Clean Code", saved.getTitle());
        verify(bookRepository).save(validBook);
    }

    @Test
    void addBook_ThrowsBadRequest_WhenAvailableCopiesExceedTotalCopies() {
        validBook.setTotalCopies(5);
        validBook.setAvailableCopies(10);

        assertThrows(BadRequestException.class, () -> bookService.addBook(validBook));
    }

    @Test
    void getBook_ThrowsResourceNotFound_WhenBookNotFound() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBook(999));
    }
}
