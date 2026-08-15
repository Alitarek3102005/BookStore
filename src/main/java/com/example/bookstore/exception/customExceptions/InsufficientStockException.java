package com.example.bookstore.exception.customExceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(){}
    public InsufficientStockException(String message) {
        super(message);
    }
}
