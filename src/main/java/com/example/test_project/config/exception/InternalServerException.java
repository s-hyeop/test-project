package com.example.test_project.config.exception;

// 500 Internal Server Error - 서버 오류
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}
