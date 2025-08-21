package com.example.test_project.dto.response;

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
public class TodoStatisticsResponse {

    @Schema(description = "전체 TODO 개수", example = "50")
    private int totalCount;

    @Schema(description = "완료된 TODO 개수", example = "30")
    private int completedCount;

    @Schema(description = "오늘 완료된 TODO 개수", example = "5")
    private int todayCompletedCount;

}
