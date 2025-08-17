package com.example.test_project.api.auth.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenResponse {

    @Schema(description = "클라이언트 OS 정보", example = "Windows 11")
    private String clientOs;

    @Schema(description = "토큰 발급 시각 (ISO-8601)", example = "2025-08-17 12:34:56")
    private LocalDateTime createdAt;
}
