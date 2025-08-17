package com.example.test_project.api.todo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoPatchRequest {

    @Schema(description = "TODO 완료 여부 (true=완료, false=미완료)", example = "true")
    private Boolean completed;

    @Schema(description = "TODO 정렬 순서", example = "2")
    private Integer sequence;

}
