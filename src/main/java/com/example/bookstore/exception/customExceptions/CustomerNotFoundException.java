package com.example.bookstore.exception.customExceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException() {}
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
