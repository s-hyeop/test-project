package com.example.test_project.api.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.api.auth.dto.request.EmailValidateRequest;
import com.example.test_project.api.auth.dto.request.LoginRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordCodeSendRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordCodeVerifyRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordRequest;
import com.example.test_project.api.auth.dto.request.SignupCodeSendRequest;
import com.example.test_project.api.auth.dto.request.SignupCodeVerifyRequest;
import com.example.test_project.api.auth.dto.request.SignupRequest;
import com.example.test_project.api.auth.dto.response.AccessTokenResponse;
import com.example.test_project.api.auth.dto.response.RefreshTokenDetailResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/auth")
@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 관리 등의 인증 관련 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 유효성 검사", description = "회원가입 시 이메일이 이미 사용 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "사용 가능한 이메일")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/email/validate")
    public ResponseEntity<Void> validateEmail(@Valid @RequestBody EmailValidateRequest emailValidateRequest) {
        // TODO: Service.validateEmail(emailValidateRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력하여 AccessToken과 RefreshToken을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // TODO: Service.login(loginRequest) 호출
        
        return ResponseEntity.ok(AccessTokenResponse.builder().build());
    }

    @Operation(summary = "회원가입 코드 발송", description = "회원가입을 위해 이메일로 인증 코드를 발송합니다.")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/signup/code")
    public ResponseEntity<Void> sendSignupCode(@Valid @RequestBody SignupCodeSendRequest signupCodeSendRequest) {
        // TODO: Service.sendSignupCode(signupCodeSendRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입 코드 검증", description = "이메일로 발송된 인증 코드를 검증합니다.")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/signup/verify")
    public ResponseEntity<Void> verifySignupCode(@Valid @RequestBody SignupCodeVerifyRequest signupCodeVerifyRequest) {
        // TODO: Service.verifySignupCode(signupCodeVerifyRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입", description = "이메일 및 인증 코드를 검증한 뒤 회원가입을 완료합니다.")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        // TODO: Service.signup(signupRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정 코드 발송", description = "비밀번호 재설정을 위해 이메일로 인증 코드를 발송합니다.")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/reset-password/code")
    public ResponseEntity<Void> sendResetPasswordCode(@Valid @RequestBody ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {
        // TODO: Service.sendResetPasswordCode(resetPasswordCodeSendRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정 코드 검증", description = "이메일로 발송된 비밀번호 재설정 코드를 검증합니다.")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/reset-password/verify")
    public ResponseEntity<Void> verifyResetPasswordCode(@Valid @RequestBody ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {
        // TODO: Service.verifyResetPasswordCode(resetPasswordCodeVerifyRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정", description = "코드 검증이 완료된 사용자의 비밀번호를 재설정합니다.")
    // @PreAuthorize("isAnonymous()") // 권한 상태가 Anonymous 인가?
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        // TODO: Service.resetPassword(resetPasswordRequest) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "RefreshToken 목록 조회", description = "로그인된 사용자의 RefreshToken 목록을 조회합니다.")
    // @PreAuthorize("hasRole('USER')") // 권한 상태가 USER 이상인가?
    @GetMapping("/tokens")
    public ResponseEntity<RefreshTokenDetailResponse> getTokens() {
        // TODO: int userNo = JWT 토큰에서 userNo 추출
        // TODO: Service.getTokens(userNo) 호출
        return ResponseEntity.ok(RefreshTokenDetailResponse.builder().build());
    }

    @Operation(summary = "AccessToken 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken을 발급받습니다.")
    // @PreAuthorize("hasRole('USER')") // 권한 상태가 USER 이상인가?
    @PostMapping("/tokens/refresh")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken() {
        // TODO: int userNo = JWT 토큰에서 userNo 추출
        // TODO: Service.refreshAccessToken(userNo, [Cookies]refreshToken) 호출
        
        return ResponseEntity.ok(AccessTokenResponse.builder().build());
    }

    @Operation(summary = "특정 RefreshToken 삭제", description = "사용자가 보유한 특정 RefreshToken을 삭제합니다.")
    // @PreAuthorize("hasRole('USER')") // 권한 상태가 USER 이상인가?
    @DeleteMapping("/tokens/{refreshToken}")
    public ResponseEntity<Void> deleteToken(@PathVariable String refreshToken) {
        // TODO: int userNo = JWT 토큰에서 userNo 추출
        // TODO: Service.deleteToken(userNo, refreshToken) 호출
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "현재 RefreshToken 삭제", description = "현재 로그인 세션에서 사용 중인 RefreshToken을 삭제합니다.")
    // @PreAuthorize("hasRole('USER')") // 권한 상태가 USER 이상인가?
    @DeleteMapping("/tokens/current")
    public ResponseEntity<Void> deleteCurrentToken() {
        // TODO: int userNo = JWT 토큰에서 userNo 추출
        // TODO: Service.deleteToken(userNo, [Cookies]refreshToken) 호출
        
        return ResponseEntity.ok().build();
    }

}
