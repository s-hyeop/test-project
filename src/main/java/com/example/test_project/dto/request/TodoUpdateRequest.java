package com.example.test_project.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoUpdateRequest {

    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    @Size(max = 1000, message = "내용은 1000자 이하여야 합니다.")
    private String content;

    @Pattern(
        regexp = "^(red|blue|green|yellow|purple)?$",
        message = "색상은 red, blue, green, yellow, purple 중 하나여야 합니다."
    )
    private String color;

    @NotNull(message = "마감일은 비어 있을 수 없습니다.")
    private LocalDate dueAt;

    @NotNull(message = "순서는 비어 있을 수 없습니다.")
    private Integer sequence;

}
