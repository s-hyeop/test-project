package com.example.test_project.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Token {
    private int tokenNo;
    private int userNo;
    private String refreshToken;
    private String clientOs;
    private String accessTokenExpiresAt;
    private int refreshTokenExpiresAt;
    private LocalDateTime createdAt;
}
