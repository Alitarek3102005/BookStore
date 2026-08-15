package com.example.bookstore.exception.customExceptions;

public class OrderException extends  RuntimeException {
    public OrderException() {
    }
    public OrderException(String message) {
        super(message);
    }
}
