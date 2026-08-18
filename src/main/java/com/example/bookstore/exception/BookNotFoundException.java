package com.example.bookstore.exception;

public class BookNotFoundException extends RuntimeException {
    public  BookNotFoundException() {}
    public BookNotFoundException(String message) {
        super(message);
    }
}
