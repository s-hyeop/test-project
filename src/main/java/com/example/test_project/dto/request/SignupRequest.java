package com.example.test_project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequest {

    @Schema(description = "회원 이메일", example = "newuser@example.com")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 300, message = "이메일은 300자 이하여야 합니다.")
    private String email;

    @Schema(description = "회원 비밀번호 (영문+숫자 필수, 특수문자 선택, 6~72자)", example = "password!")
    @NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
    @Size(min = 6, max = 72, message = "비밀번호는 6자 이상 72자 이하여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]{6,72}$",
        message = "비밀번호는 영문과 숫자를 반드시 포함해야 하며, 특수문자는 선택적으로 사용할 수 있습니다."
    )
    private String password;

    @Schema(description = "회원 이름 (2~30자)", example = "홍길동")
    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하여야 합니다.")
    private String userName;

    @Schema(description = "회원가입 인증 코드 (숫자 6자리)", example = "123456")
    @NotBlank(message = "인증 코드는 비어 있을 수 없습니다.")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 숫자 6자리여야 합니다.")
    private String code;

}
