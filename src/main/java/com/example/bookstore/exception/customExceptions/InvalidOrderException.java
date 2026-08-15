package com.example.bookstore.exception.customExceptions;

public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(){}
    public InvalidOrderException(String message) {
        super(message);
    }
}
