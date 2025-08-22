package com.example.test_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordCodeVerifyRequest {

    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 300, message = "이메일은 300자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "인증 코드는 비어 있을 수 없습니다.")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 숫자 6자리여야 합니다.")
    private String code;

}
