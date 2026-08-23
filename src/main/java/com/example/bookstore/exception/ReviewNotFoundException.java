package com.example.bookstore.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException() {}
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
