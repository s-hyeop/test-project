package com.example.test_project.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {
    private String code;       // 에러 코드
    private String message;    // 에러 메시지
    private Object details;    // 추가 상세 정보
}
