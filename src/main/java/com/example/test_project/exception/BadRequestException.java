package com.example.test_project.exception;

// 400 Bad Request - 잘못된 요청
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
