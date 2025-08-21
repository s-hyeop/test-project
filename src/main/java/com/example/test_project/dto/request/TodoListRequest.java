package com.example.test_project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoListRequest {

    @Schema(description = "TODO 상태", allowableValues = {"all", "complete", "incomplete"}, example = "complete")
    @Pattern(
        regexp = "^(all|complete|incomplete)$",
        message = "status 값은 all, complete, incomplete 중 하나여야 합니다."
    )
    private String status;

    @Schema(description = "검색 조건 (제목 또는 내용)", allowableValues = {"title", "content"}, example = "title")
    @Pattern(
        regexp = "^(title|content)?$",
        message = "searchType 값은 title, content 중 하나여야 합니다."
    )
    private String searchType;

    @Schema(description = "검색 키워드 (최대 100자)", example = "운동")
    @Size(max = 100, message = "검색어는 100자 이하여야 합니다.")
    private String keyword;

    @Schema(description = "페이지 번호 (1 이상)", example = "1", defaultValue = "1")
    @Builder.Default
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    private int page = 1;

    @Schema(description = "페이지 크기 (10~50)", example = "10", defaultValue = "10")
    @Builder.Default
    @Min(value = 10, message = "페이지 크기는 10 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하이어야 합니다.")
    private int size = 10;

}
