package com.example.test_project.api.user.dto.response;

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
public class UserDetailResponse {
    @Schema(description = "회원 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "회원 이름", example = "홍길동")
    private String userName;

    @Schema(description = "회원 가입 일자 (ISO-8601)", example = "2025-08-17T10:30:00")
    private LocalDateTime createdAt;
}
