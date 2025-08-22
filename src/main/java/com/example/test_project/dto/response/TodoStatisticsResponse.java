package com.example.test_project.dto.response;

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
public class TodoStatisticsResponse {

    // 전체 TODO 개수
    private int totalCount;

    // 완료된 TODO 개수
    private int completedCount;

    // 오늘 완료된 TODO 개수
    private int todayCompletedCount;

}
