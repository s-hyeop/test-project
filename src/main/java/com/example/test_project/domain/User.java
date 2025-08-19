package com.example.test_project.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private int userNo;
    private String email;
    private String password;
    private String userName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private LocalDateTime lastLoginAt;
}
