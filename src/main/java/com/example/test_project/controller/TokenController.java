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
import com.example.test_project.config.exception.UnauthorizedException;
import com.example.test_project.config.properties.AppProperties;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.AuthService;
import com.example.test_project.util.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 토큰 관리 REST API 컨트롤러
 * 
 * <p>Refresh Token과 Access Token 관리를 위한 엔드포인트를 제공합니다.
 * JWT 토큰 갱신, 조회, 삭제 기능을 담당합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final AuthService authService;
    private final RateLimitUtil rateLimitUtil;
    private final AppProperties appProperties;

    /**
     * 현재 사용자의 모든 활성 토큰 목록을 조회합니다.
     * 
     * <p>여러 기기에서 로그인한 경우 각 기기별 토큰 정보를 확인할 수 있습니다.</p>
     * 
     * @return 활성 Refresh Token 목록
     */
    @GetMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RefreshTokenDetailResponse>> getTokens() {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }
        log.info("토큰 목록 조회 요청 - userNo: {}", userNo);

        List<RefreshTokenDetailResponse> list = authService.getTokens(userNo);
        log.debug("토큰 목록 조회 완료 - userNo: {}, 토큰 수: {}", userNo, list.size());

        return ResponseEntity.ok().body(list);
    }


    /**
     * Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.
     * 
     * <p>Access Token 만료 시 Refresh Token을 사용하여 새로운 Access Token을 발급받을 수 있습니다.
     * Rate limiting이 적용되어 과도한 갱신 요청이 제한됩니다.</p>
     * 
     * @param refreshToken 쿠키에서 읽은 Refresh Token
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 새로운 Access Token
     * @throws BadRequestException Refresh Token이 없거나 유효하지 않은 경우
     */
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(
            @CookieValue(name = "${app.jwt-refresh-token-cookie-name}", required = false) String refreshToken, 
            HttpServletRequest request) {

        rateLimitUtil.checkRateLimit(request);
        log.info("Access Token 갱신 요청");

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Access Token 갱신 실패 - Refresh Token이 없음");
            throw new BadRequestException("올바르지 않은 토큰입니다.");
        }

        log.debug("Access Token 갱신 시도 - refreshToken: {}", refreshToken);
        AccessTokenResponse accessTokenResponse = authService.refreshAccessToken(refreshToken);
        log.info("Access Token 갱신 성공");

        return ResponseEntity.ok().body(accessTokenResponse);
    }


    /**
     * 특정 Refresh Token을 삭제합니다.
     * 
     * <p>다른 기기에서의 로그인 세션을 종료할 때 사용합니다.</p>
     * 
     * @param refreshToken 삭제할 Refresh Token
     * @return 204 No Content
     */
    @DeleteMapping("/{refreshToken}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteToken(@PathVariable String refreshToken) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("토큰 삭제 요청 - userNo: {}, refreshToken: {}", userNo, refreshToken);
        authService.deleteToken(userNo, refreshToken);
        log.info("토큰 삭제 완료 - userNo: {}", userNo);

        return ResponseEntity.noContent().build();
    }


    /**
     * 현재 세션의 Refresh Token을 삭제합니다 (로그아웃).
     * 
     * <p>현재 기기에서 로그아웃할 때 사용합니다.
     * 쿠키에서 Refresh Token을 삭제하고 서버에서도 토큰을 무효화합니다.</p>
     * 
     * @param refreshToken 쿠키에서 읽은 현재 세션의 Refresh Token
     * @return 204 No Content와 함께 쿠키 삭제 헤더
     */
    @DeleteMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCurrentToken(@CookieValue("${app.jwt-refresh-token-cookie-name}") String refreshToken) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("현재 토큰 삭제(로그아웃) 요청 - userNo: {}", userNo);
        authService.deleteToken(userNo, refreshToken);
        log.info("현재 토큰 삭제 완료 - userNo: {}", userNo);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, removeRefreshTokenCookie().toString())
                .build();
    }


    /**
     * Refresh Token 쿠키를 제거하는 ResponseCookie를 생성합니다.
     * 
     * @return maxAge가 0으로 설정된 쿠키 (브라우저에서 삭제됨)
     */
    private ResponseCookie removeRefreshTokenCookie() {
        log.debug("Refresh Token 쿠키 제거 설정 생성");
        return ResponseCookie.from(appProperties.getJwtRefreshTokenCookieName(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }

}
