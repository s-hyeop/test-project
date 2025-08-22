package com.example.test_project.config.exception;

// 401 Unauthorized - 인증 실패
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
