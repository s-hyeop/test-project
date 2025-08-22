package com.example.test_project.config.exception;

// 404 Not Found - 리소스 없음
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
