package com.example.test_project.api.todo.dto.response;

import java.time.LocalDate;
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
public class TodoDetailResponse {

    @Schema(description = "TODO ID", example = "********-****-****-****-************")
    private String todoId;

    @Schema(description = "TODO 제목", example = "공부하기")
    private String title;

    @Schema(description = "TODO 내용", example = "공부하기 내용")
    private String content;

    @Schema(description = "색상", allowableValues = {"red", "blue", "green", "yellow", "purple"}, example = "red")
    private String color;

    @Schema(description = "정렬 순서", example = "1")
    private int sequence;

    @Schema(description = "마감일", example = "2025-12-31")
    private LocalDate dueAt;

    @Schema(description = "완료일 (완료 시각, null 가능)", example = "2025-08-17 15:00:00")
    private LocalDateTime completedAt;

    @Schema(description = "생성일", example = "2025-08-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-08-10 14:00:00")
    private LocalDateTime updatedAt;

}
