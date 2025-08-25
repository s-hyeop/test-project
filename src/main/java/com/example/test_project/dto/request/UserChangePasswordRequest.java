package com.example.test_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserChangePasswordRequest {

    @NotBlank(message = "현재 비밀번호는 비어 있을 수 없습니다.")
    private String password;

    @NotBlank(message = "새로운 비밀번호는 비어 있을 수 없습니다.")
    @Size(min = 6, max = 72, message = "비밀번호는 6자 이상 72자 이하여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]{6,72}$",
        message = "새로운 비밀번호는 영문과 숫자를 반드시 포함해야 하며, 특수문자는 선택적으로 사용할 수 있습니다."
    )
    private String newPassword;

}
