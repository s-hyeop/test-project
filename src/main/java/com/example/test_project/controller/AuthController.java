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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@PreAuthorize("!isAuthenticated()")
public class AuthController {

    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;
    private final AppProperties appProperties;


    @PostMapping("/email/exist")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<EmailExistResponse> checkEmailExist(@Valid @RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        EmailExistResponse emailExistResponse = authService.existsByEmail(emailRequest);
        return ResponseEntity.ok().body(emailExistResponse);
    }



    @PostMapping("/login")
    @PreAuthorize("!isAuthenticated()")
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
    public ResponseEntity<Void> sendSignupCode(@Valid @RequestBody SignupCodeSendRequest signupCodeSendRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.sendSignupCode(signupCodeSendRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/signup/verify")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> verifySignupCode(@Valid @RequestBody SignupCodeVerifyRequest signupCodeVerifyRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.verifySignupCode(signupCodeVerifyRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/signup")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.signup(signupRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/reset-password/code")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> sendResetPasswordCode(@Valid @RequestBody ResetPasswordCodeSendRequest resetPasswordCodeSendRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.sendResetPasswordCode(resetPasswordCodeSendRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/reset-password/verify")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<Void> verifyResetPasswordCode(@Valid @RequestBody ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        authService.verifyResetPasswordCode(resetPasswordCodeVerifyRequest);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/reset-password")
    @PreAuthorize("!isAuthenticated()")
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
