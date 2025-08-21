package com.example.test_project.dto.response;

import java.util.List;

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
public class TodoListResponse {

    @Schema(description = "현재 페이지")
    private int page;

    @Schema(description = "페이지 크기")
    private int size;

    @Schema(description = "전체 개수")
    private int totalCount;

    @Schema(description = "목록")
    private List<TodoDetailResponse> list;
}
