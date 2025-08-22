package com.example.test_project.dto.response;

import java.time.LocalDate;
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
public class TodoDetailResponse {

    // TODO ID
    private String todoId;

    // TODO 제목
    private String title;

    // TODO 내용
    private String content;

    // 색상
    private String color;

    // 정렬 순서
    private int sequence;

    // 마감일
    private LocalDate dueAt;

    // 완료일 (완료 시각, null 가능)
    private LocalDateTime completedAt;

    // 생성일
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;

}
