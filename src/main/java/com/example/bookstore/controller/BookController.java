package com.example.bookstore.controller;

import com.example.bookstore.api.BooksApi;
import com.example.bookstore.dto.BookPatchRequest;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BookController implements BooksApi {

    private final BookService bookService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> createBook(BookRequest bookRequest) {
        return new ResponseEntity<>(bookService.addBook(bookRequest), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(UUID bookId) {
        bookService.deleteBook(bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<BookResponse>> getAllBooks(String title, String author, UUID categoryId, Boolean active, Integer page, Integer size, String sort) {
        List<BookResponse> books = bookService.searchBooks(title, author, categoryId, active, page, size, sort);
        return ResponseEntity.ok(books);
    }


    @Override
    public ResponseEntity<BookResponse> getBookById(UUID bookId) {
        return new ResponseEntity<>(bookService.getById(bookId), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> patchBook(UUID bookId, BookPatchRequest bookPatchRequest) {
        return new ResponseEntity<>(bookService.patchBook(bookId, bookPatchRequest), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBook(UUID bookId, BookRequest bookRequest) {
        return new ResponseEntity<>(bookService.updateBook(bookId, bookRequest), HttpStatus.OK);
    }
}