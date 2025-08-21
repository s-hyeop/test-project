package com.example.test_project.exception;

// 409 Conflict - 중복 등 충돌
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
