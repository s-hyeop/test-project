package com.example.test_project.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenDetailResponse {

    private String refreshToken;

    // 클라이언트 OS 정보
    private String clientOs;

    // 토큰 발급 시각
    private LocalDateTime createdAt;
}
