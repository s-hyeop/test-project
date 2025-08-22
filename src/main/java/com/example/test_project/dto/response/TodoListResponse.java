package com.example.test_project.dto.response;

import java.util.List;

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
public class TodoListResponse {

    // 현재 페이지
    private int page;

    // 페이지 크기
    private int size;

    // 전체 개수
    private int totalCount;

    // 목록
    private List<TodoDetailResponse> list;
}
