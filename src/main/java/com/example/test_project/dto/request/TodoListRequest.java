package com.example.test_project.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoListRequest {

    @Pattern(
        regexp = "^(all|complete|incomplete)$",
        message = "status 값은 all, complete, incomplete 중 하나여야 합니다."
    )
    private String status;

    @Pattern(
        regexp = "^(title|content)?$",
        message = "searchType 값은 title, content 중 하나여야 합니다."
    )
    private String searchType;

    @Size(max = 100, message = "검색어는 100자 이하여야 합니다.")
    private String keyword;

    @Builder.Default
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    private int page = 1;

    @Builder.Default
    @Min(value = 10, message = "페이지 크기는 10 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하이어야 합니다.")
    private int size = 10;

}
