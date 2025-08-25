package com.example.test_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.config.exception.UnauthorizedException;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.UserService;
import com.example.test_project.util.AuthUtil;
import com.example.test_project.util.RateLimitUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 사용자 정보 관리 REST API 컨트롤러
 * 
 * <p>사용자 프로필 조회 및 수정, 비밀번호 변경 등의 기능을 제공합니다.
 * 모든 엔드포인트는 USER 권한이 필요합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;
    private final RateLimitUtil rateLimitUtil;

    /**
     * 현재 로그인한 사용자의 상세 정보를 조회합니다.
     * 
     * <p>이메일, 사용자명, 가입일시 등의 정보를 반환합니다.</p>
     * 
     * @return 사용자 상세 정보
     */
    @GetMapping("")
    public ResponseEntity<UserDetailResponse> getUserDetail() {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("사용자 정보 조회 요청 - userNo: {}", userNo);
        UserDetailResponse userDetailResponse = userService.getUserDetail(userNo);
        log.debug("사용자 정보 조회 완료 - userNo: {}, email: {}", userNo, userDetailResponse.getEmail());

        return ResponseEntity.ok().body(userDetailResponse);
    }


    /**
     * 사용자 정보를 수정합니다.
     * 
     * <p>사용자명 등의 기본 정보를 수정할 수 있습니다.
     * 비밀번호 변경은 별도의 엔드포인트를 사용해야 합니다.</p>
     * 
     * @param userPatchRequest 사용자 정보 수정 요청
     * @return 204 No Content
     */
    @PatchMapping("")
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserPatchRequest userPatchRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("사용자 정보 수정 요청 - userNo: {}, userName: {}", userNo, userPatchRequest.getUserName());
        userService.updateUser(userNo, userPatchRequest);
        log.info("사용자 정보 수정 완료 - userNo: {}", userNo);

        return ResponseEntity.noContent().build();
    }


    /**
     * 사용자의 비밀번호를 변경합니다.
     * 
     * <p>현재 비밀번호 확인 후 새로운 비밀번호로 변경합니다.
     * Rate limiting이 적용되어 무차별 대입 공격을 방지합니다.</p>
     * 
     * @param userChangePasswordRequest 비밀번호 변경 요청 (현재 비밀번호, 새 비밀번호)
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 204 No Content
     */
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest,
            HttpServletRequest request) {

        rateLimitUtil.checkRateLimit(request);
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("비밀번호 변경 요청 - userNo: {}", userNo);
        userService.changePassword(userNo, userChangePasswordRequest);
        log.info("비밀번호 변경 완료 - userNo: {}", userNo);

        return ResponseEntity.noContent().build();
    }

}
