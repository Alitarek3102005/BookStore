package com.example.bookstore.errorhandling;

public class ErrorItem {
    public String message;
    public String code;

    public ErrorItem() {}

    public ErrorItem(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
