package com.example.test_project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.config.exception.BadRequestException;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.AuthService;
import com.example.test_project.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
@Tag(name = "토큰 API", description = "재발급 및 삭제 관련 API")
public class TokenController {

    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;

    @Value("${jwt.refresh-token-cookie-name:refreshToken}")
    private String rtCookieName;



    @GetMapping("")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "RefreshToken 목록 조회", description = "로그인된 사용자의 RefreshToken 목록을 조회합니다.")
    public ResponseEntity<List<RefreshTokenDetailResponse>> getTokens() {
        Integer userNo = AuthUtil.currentUserNo();

        List<RefreshTokenDetailResponse> list = authService.getTokens(userNo);
        return ResponseEntity.ok().body(list);
    }



    @PostMapping("/refresh")
    @Operation(summary = "AccessToken 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken을 발급받습니다.")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(@CookieValue(name = "${jwt.refresh-token-cookie-name}", required = false) String refreshToken, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);


        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("올바르지 않은 토큰입니다.");
        }

        AccessTokenResponse accessTokenResponse = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok()
            .body(accessTokenResponse);
    }



    @DeleteMapping("/{refreshToken}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "특정 RefreshToken 삭제", description = "사용자가 보유한 특정 RefreshToken을 삭제합니다.")
    public ResponseEntity<Void> deleteToken(@PathVariable String refreshToken) {
        Integer userNo = AuthUtil.currentUserNo();

        authService.deleteToken(userNo, refreshToken);
        return ResponseEntity.noContent().build();
    }



    @DeleteMapping("/current")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "현재 RefreshToken 삭제", description = "현재 로그인 세션에서 사용 중인 RefreshToken을 삭제합니다.")
    public ResponseEntity<Void> deleteCurrentToken(@CookieValue("${jwt.refresh-token-cookie-name}") String refreshToken) {
        Integer userNo = AuthUtil.currentUserNo();
        
        authService.deleteToken(userNo, refreshToken);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, removeRefreshTokenCookie().toString())
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
