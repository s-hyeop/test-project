package com.example.test_project.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validation;
    private String path;
}
