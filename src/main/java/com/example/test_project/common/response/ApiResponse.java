package com.example.test_project.common.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private boolean success; // 성공 여부
    private String message; // 응답 메시지

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data; // 성공시 데이터

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ApiError error; // 실패시 에러 정보

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path; // 응답 URI 

    private OffsetDateTime timestamp; // 응답 시각
}
