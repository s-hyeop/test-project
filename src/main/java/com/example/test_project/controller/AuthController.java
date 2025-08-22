package com.example.test_project.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.config.properties.AppRroperties;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.AuthService;
import com.example.test_project.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@PreAuthorize("!isAuthenticated()")
@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 관리 등의 인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;
    private final AppRroperties appProperties;


    @PostMapping("/email/exist")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "이메일 유효성 검사", description = "회원가입 시 이메일이 이미 사용 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "사용 가능한 이메일")
    public ResponseEntity<EmailExistResponse> checkEmailExist(@Valid @RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        EmailExistResponse emailExistResponse = authService.existsByEmail(emailRequest);
        return ResponseEntity.ok().body(emailExistResponse);
    }



    @PostMapping("/login")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력하여 Body로 AccessToken과 Cookie로 RefreshToken을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest req, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        AccessTokenResponse accessTokenResponse = authService.login(loginRequest, OsDetectorUtil.detect(req));
        ResponseCookie tokenCookie = generateRefreshTokenCookie(accessTokenResponse.getRefreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
            .body(accessTokenResponse);
    }



    @PostMapping("/signup/code")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "회원가입 코드 발송", description = "회원가입을 위해 이메일로 인증 코드를 발송합니다.")
    public ResponseEntity<Void> sendSignupCode(@Valid @RequestBody SignupCodeSendRequest signupCodeSendRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.sendSignupCode(signupCodeSendRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/signup/verify")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "회원가입 코드 검증", description = "이메일로 발송된 인증 코드를 검증합니다.")
    public ResponseEntity<Void> verifySignupCode(@Valid @RequestBody SignupCodeVerifyRequest signupCodeVerifyRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.verifySignupCode(signupCodeVerifyRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/signup")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "회원가입", description = "이메일 및 인증 코드를 검증한 뒤 회원가입을 완료합니다.")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.signup(signupRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/reset-password/code")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "비밀번호 재설정 코드 발송", description = "비밀번호 재설정을 위해 이메일로 인증 코드를 발송합니다.")
    public ResponseEntity<Void> sendResetPasswordCode(@Valid @RequestBody ResetPasswordCodeSendRequest resetPasswordCodeSendRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.sendResetPasswordCode(resetPasswordCodeSendRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/reset-password/verify")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "비밀번호 재설정 코드 검증", description = "이메일로 발송된 비밀번호 재설정 코드를 검증합니다.")
    public ResponseEntity<Void> verifyResetPasswordCode(@Valid @RequestBody ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.verifyResetPasswordCode(resetPasswordCodeVerifyRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/reset-password")
    @PreAuthorize("!isAuthenticated()")
    @Operation(summary = "비밀번호 재설정", description = "코드 검증이 완료된 사용자의 비밀번호를 재설정합니다.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }


    private ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(appProperties.getJwtRefreshTokenCookieName(), refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofMinutes(appProperties.getJwtRefreshExpirationMinutes()))
            .build();
    }

}
