package com.example.bookstore.exception.customExceptions;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(){

    }
    public DuplicateResourceException(String message) {
        super(message);
    }
}
