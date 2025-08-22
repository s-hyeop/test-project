package com.example.test_project.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.config.properties.AppProperties;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.AuthService;
import com.example.test_project.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * 인증 관련 REST API 컨트롤러
 * 
 * <p>회원가입, 로그인, 비밀번호 재설정 등 인증 관련 엔드포인트를 제공합니다.
 * 모든 엔드포인트는 인증되지 않은 사용자만 접근 가능합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@PreAuthorize("!isAuthenticated()")
public class AuthController {

    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;
    private final AppProperties appProperties;

    /**
     * 이메일 중복 확인을 수행합니다.
     * 
     * <p>회원가입 시 이메일이 이미 사용 중인지 확인합니다.
     * Rate limiting이 적용되어 무차별 대입 공격을 방지합니다.</p>
     * 
     * @param emailRequest 확인할 이메일 정보
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 이메일 존재 여부
     */
    @PostMapping("/email/exist")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<EmailExistResponse> checkEmailExist(@Valid @RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("이메일 중복 확인 요청 - email: {}", emailRequest.getEmail());

        EmailExistResponse emailExistResponse = authService.existsByEmail(emailRequest);
        log.debug("이메일 중복 확인 완료 - email: {}, exists: {}", emailRequest.getEmail(), emailExistResponse.isExists());

        return ResponseEntity.ok().body(emailExistResponse);
    }


    /**
     * 사용자 로그인을 처리합니다.
     * 
     * <p>이메일과 비밀번호를 검증하고 JWT 토큰을 발급합니다.
     * Access Token은 응답 본문에, Refresh Token은 쿠키로 전달됩니다.</p>
     * 
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @param req HTTP 요청 객체 (OS 감지용)
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return Access Token과 Refresh Token
     */
    @PostMapping("/login")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        String clientOs = OsDetectorUtil.detect(request);
        log.info("로그인 시도 - email: {}, clientOs: {}", loginRequest.getEmail(), clientOs);

        AccessTokenResponse accessTokenResponse = authService.login(loginRequest, clientOs);
        ResponseCookie tokenCookie = generateRefreshTokenCookie(accessTokenResponse.getRefreshToken());

        log.info("로그인 성공 - email: {}", loginRequest.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
                .body(accessTokenResponse);
    }


    /**
     * 회원가입용 인증 코드를 이메일로 발송합니다.
     * 
     * <p>6자리 인증 코드를 생성하여 이메일로 전송합니다.
     * 인증 코드는 Redis에 임시 저장됩니다.</p>
     * 
     * @param signupCodeSendRequest 인증 코드를 받을 이메일 정보
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PostMapping("/signup/code")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> sendSignupCode(@Valid @RequestBody SignupCodeSendRequest signupCodeSendRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("회원가입 인증 코드 발송 요청 - email: {}", signupCodeSendRequest.getEmail());

        authService.sendSignupCode(signupCodeSendRequest);
        log.info("회원가입 인증 코드 발송 완료 - email: {}", signupCodeSendRequest.getEmail());

        return ResponseEntity.noContent().build();
    }


    /**
     * 회원가입용 인증 코드를 검증합니다.
     * 
     * <p>이메일로 발송된 인증 코드가 올바른지 확인합니다.</p>
     * 
     * @param signupCodeVerifyRequest 이메일과 인증 코드 정보
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PostMapping("/signup/verify")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> verifySignupCode(@Valid @RequestBody SignupCodeVerifyRequest signupCodeVerifyRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("회원가입 인증 코드 검증 요청 - email: {}", signupCodeVerifyRequest.getEmail());

        authService.verifySignupCode(signupCodeVerifyRequest);
        log.info("회원가입 인증 코드 검증 성공 - email: {}", signupCodeVerifyRequest.getEmail());

        return ResponseEntity.noContent().build();
    }


    /**
     * 회원가입을 처리합니다.
     * 
     * <p>인증 코드 검증 후 새로운 사용자 계정을 생성합니다.
     * 비밀번호는 bcrypt로 해싱하여 저장됩니다.</p>
     * 
     * @param signupRequest 회원가입 정보 (이메일, 비밀번호, 사용자명, 인증코드)
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PostMapping("/signup")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest,  HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("회원가입 요청 - email: {}, userName: {}", signupRequest.getEmail(), signupRequest.getUserName());

        authService.signup(signupRequest);
        log.info("회원가입 완료 - email: {}", signupRequest.getEmail());

        return ResponseEntity.noContent().build();
    }


    /**
     * 비밀번호 재설정용 인증 코드를 이메일로 발송합니다.
     * 
     * <p>등록된 이메일로 6자리 인증 코드를 전송합니다.</p>
     * 
     * @param resetPasswordCodeSendRequest 인증 코드를 받을 이메일 정보
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PostMapping("/reset-password/code")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> sendResetPasswordCode(@Valid @RequestBody ResetPasswordCodeSendRequest resetPasswordCodeSendRequest,  HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("비밀번호 재설정 인증 코드 발송 요청 - email: {}", resetPasswordCodeSendRequest.getEmail());

        authService.sendResetPasswordCode(resetPasswordCodeSendRequest);
        log.info("비밀번호 재설정 인증 코드 발송 완료 - email: {}", resetPasswordCodeSendRequest.getEmail());

        return ResponseEntity.noContent().build();
    }


    /**
     * 비밀번호 재설정용 인증 코드를 검증합니다.
     * 
     * @param resetPasswordCodeVerifyRequest 이메일과 인증 코드 정보
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PostMapping("/reset-password/verify")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> verifyResetPasswordCode(@Valid @RequestBody ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("비밀번호 재설정 인증 코드 검증 요청 - email: {}", resetPasswordCodeVerifyRequest.getEmail());

        authService.verifyResetPasswordCode(resetPasswordCodeVerifyRequest);
        log.info("비밀번호 재설정 인증 코드 검증 성공 - email: {}", resetPasswordCodeVerifyRequest.getEmail());

        return ResponseEntity.noContent().build();
    }


    /**
     * 비밀번호를 재설정합니다.
     * 
     * <p>인증 코드 검증 후 새로운 비밀번호로 변경합니다.</p>
     * 
     * @param resetPasswordRequest 비밀번호 재설정 정보 (이메일, 새 비밀번호, 인증코드)
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PostMapping("/reset-password")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        log.info("비밀번호 재설정 요청 - email: {}", resetPasswordRequest.getEmail());

        authService.resetPassword(resetPasswordRequest);
        log.info("비밀번호 재설정 완료 - email: {}", resetPasswordRequest.getEmail());

        return ResponseEntity.noContent().build();
    }


    /**
     * Refresh Token 쿠키를 생성합니다.
     * 
     * <p>HttpOnly, Secure, SameSite 속성이 설정된 쿠키를 생성합니다.</p>
     * 
     * @param refreshToken Refresh Token 값
     * @return 설정된 ResponseCookie
     */
    private ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        log.debug("Refresh Token 쿠키 생성 - 만료시간: {}분", appProperties.getJwtRefreshExpirationMinutes());

        return ResponseCookie.from(appProperties.getJwtRefreshTokenCookieName(), refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(appProperties.getJwtRefreshExpirationMinutes()))
                .build();
    }

}
