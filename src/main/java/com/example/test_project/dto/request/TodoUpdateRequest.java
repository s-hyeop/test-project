package com.example.test_project.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoUpdateRequest {

    @Schema(description = "TODO 제목 (최대 100자)", example = "공부하기")
    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @Schema(description = "TODO 내용 (최대 1000자)", example = "Spring Boot 공부하기")
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    @Size(max = 1000, message = "내용은 1000자 이하여야 합니다.")
    private String content;

    @Schema(description = "색상 (선택, 지정된 값만 허용)", allowableValues = {"red", "blue", "green", "yellow", "purple"}, example = "red")
    @Pattern(
        regexp = "^(red|blue|green|yellow|purple)?$",
        message = "색상은 red, blue, green, yellow, purple 중 하나여야 합니다."
    )
    private String color;

    @Schema(description = "마감일", example = "2025-12-31")
    @NotNull(message = "마감일은 비어 있을 수 없습니다.")
    private LocalDate dueAt;

}
