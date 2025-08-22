package com.example.test_project.controller;

import java.util.List;

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
import com.example.test_project.config.properties.AppProperties;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.AuthService;
import com.example.test_project.util.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;

    private final AppProperties appProperties;

    @GetMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RefreshTokenDetailResponse>> getTokens() {
        Integer userNo = AuthUtil.currentUserNo();

        List<RefreshTokenDetailResponse> list = authService.getTokens(userNo);
        return ResponseEntity.ok().body(list);
    }



    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(@CookieValue(name = "${app.jwt-refresh-token-cookie-name}", required = false) String refreshToken, HttpServletRequest request) {
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
    public ResponseEntity<Void> deleteToken(@PathVariable String refreshToken) {
        Integer userNo = AuthUtil.currentUserNo();

        authService.deleteToken(userNo, refreshToken);
        return ResponseEntity.noContent().build();
    }



    @DeleteMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCurrentToken(@CookieValue("${app.jwt-refresh-token-cookie-name}") String refreshToken) {
        Integer userNo = AuthUtil.currentUserNo();
        
        authService.deleteToken(userNo, refreshToken);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, removeRefreshTokenCookie().toString())
            .build();
    }

    private ResponseCookie removeRefreshTokenCookie() {
        return ResponseCookie.from(appProperties.getJwtRefreshTokenCookieName(), "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();
    }

}
