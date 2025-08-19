package com.example.test_project.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Todo {
    private String todoId;
    private int userNo;
    private String title;
    private String Content;
    private String color;
    private int sequence;
    private LocalDate dueAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
