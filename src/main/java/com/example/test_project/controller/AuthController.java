package com.example.test_project.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.dto.request.EmailRequest;
import com.example.test_project.dto.request.LoginRequest;
import com.example.test_project.dto.request.ResetPasswordCodeSendRequest;
import com.example.test_project.dto.request.ResetPasswordCodeVerifyRequest;
import com.example.test_project.dto.request.ResetPasswordRequest;
import com.example.test_project.dto.request.SignupCodeSendRequest;
import com.example.test_project.dto.request.SignupCodeVerifyRequest;
import com.example.test_project.dto.request.SignupRequest;
import com.example.test_project.dto.response.AccessTokenResponse;
import com.example.test_project.dto.response.EmailExistResponse;
import com.example.test_project.dto.response.RefreshTokenDetailResponse;
import com.example.test_project.service.AuthService;
import com.example.test_project.util.AuthUtil;
import com.example.test_project.util.OsDetectorUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Value("${jwt.refresh-token-cookie-name:refreshToken}")
    private String rtCookieName;

    @Value("${jwt.access-expiration-minutes}")
    private int accessExpirationMinutes;

    @Value("${jwt.refresh-expiration-minutes}")
    private int refreshExpirationMinutes;

    @PostMapping("/email/exist")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "이메일 유효성 검사", description = "회원가입 시 이메일이 이미 사용 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "사용 가능한 이메일")
    public ResponseEntity<EmailExistResponse> checkEmailExist(@Valid @RequestBody EmailRequest emailRequest) {
        EmailExistResponse emailExistResponse = authService.existsByEmail(emailRequest);
        return ResponseEntity.ok().body(emailExistResponse);
    }



    @PostMapping("/login")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력하여 Body로 AccessToken과 Cookie로 RefreshToken을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest req) {
        AccessTokenResponse accessTokenResponse = authService.login(loginRequest, OsDetectorUtil.detect(req));
        ResponseCookie tokenCookie = generateRefreshTokenCookie(accessTokenResponse.getRefreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
            .body(accessTokenResponse);
    }



    @PostMapping("/signup/code")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "회원가입 코드 발송", description = "회원가입을 위해 이메일로 인증 코드를 발송합니다.")
    public ResponseEntity<Void> sendSignupCode(@Valid @RequestBody SignupCodeSendRequest signupCodeSendRequest) {
        authService.sendSignupCode(signupCodeSendRequest);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/signup/verify")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "회원가입 코드 검증", description = "이메일로 발송된 인증 코드를 검증합니다.")
    public ResponseEntity<Void> verifySignupCode(@Valid @RequestBody SignupCodeVerifyRequest signupCodeVerifyRequest) {
        authService.verifySignupCode(signupCodeVerifyRequest);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/signup")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "회원가입", description = "이메일 및 인증 코드를 검증한 뒤 회원가입을 완료합니다.")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/reset-password/code")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "비밀번호 재설정 코드 발송", description = "비밀번호 재설정을 위해 이메일로 인증 코드를 발송합니다.")
    public ResponseEntity<Void> sendResetPasswordCode(@Valid @RequestBody ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {
        authService.sendResetPasswordCode(resetPasswordCodeSendRequest);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/reset-password/verify")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "비밀번호 재설정 코드 검증", description = "이메일로 발송된 비밀번호 재설정 코드를 검증합니다.")
    public ResponseEntity<Void> verifyResetPasswordCode(@Valid @RequestBody ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {
        authService.verifyResetPasswordCode(resetPasswordCodeVerifyRequest);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/reset-password")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "비밀번호 재설정", description = "코드 검증이 완료된 사용자의 비밀번호를 재설정합니다.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok().build();
    }



    @GetMapping("/tokens")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "RefreshToken 목록 조회", description = "로그인된 사용자의 RefreshToken 목록을 조회합니다.")
    public ResponseEntity<List<RefreshTokenDetailResponse>> getTokens() {
        Integer userNo = AuthUtil.currentUserNo();

        List<RefreshTokenDetailResponse> list = authService.getTokens(userNo);
        return ResponseEntity.ok().body(list);
    }



    @PostMapping("/tokens/refresh")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AccessToken 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken을 발급받습니다.")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(@CookieValue("${jwt.refresh-token-cookie-name}") String refreshToken) {
        AccessTokenResponse accessTokenResponse = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok()
            .body(accessTokenResponse);
    }



    @DeleteMapping("/tokens/{refreshToken}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "특정 RefreshToken 삭제", description = "사용자가 보유한 특정 RefreshToken을 삭제합니다.")
    public ResponseEntity<Void> deleteToken(@PathVariable String refreshToken) {
        Integer userNo = AuthUtil.currentUserNo();

        authService.deleteToken(userNo, refreshToken);
        return ResponseEntity.ok().build();
    }



    @DeleteMapping("/tokens/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "현재 RefreshToken 삭제", description = "현재 로그인 세션에서 사용 중인 RefreshToken을 삭제합니다.")
    public ResponseEntity<Void> deleteCurrentToken(@CookieValue("${jwt.refresh-token-cookie-name}") String refreshToken) {
        Integer userNo = AuthUtil.currentUserNo();
        
        authService.deleteToken(userNo, refreshToken);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, removeRefreshTokenCookie().toString())
            .build();
    }



    private ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(rtCookieName, refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofMinutes(refreshExpirationMinutes))
            .build();
    }

    private ResponseCookie removeRefreshTokenCookie() {
        return ResponseCookie.from(rtCookieName, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();
    }

}
