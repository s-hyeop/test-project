package com.example.test_project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPatchRequest {

    @Schema(description = "회원 이름 (2~30자)", example = "홍길동")
    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하여야 합니다.")
    private String userName;

}
