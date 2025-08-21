package com.example.test_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.dto.request.UserChangePasswordRequest;
import com.example.test_project.dto.request.UserPatchRequest;
import com.example.test_project.dto.response.UserDetailResponse;
import com.example.test_project.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@Tag(name = "사용자 API", description = "사용자 정보 조회 및 수정, 비밀번호 변경 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;



    @Operation(summary = "사용자 상세 조회", description = "로그인한 사용자의 상세 정보를 조회합니다. JWT 토큰에서 userNo를 추출하여 사용합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 사용자 정보 조회됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @GetMapping("")
    public ResponseEntity<UserDetailResponse> getUserDetail() {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        UserDetailResponse userDetailResponse = userService.getUserDetail(userNo);
        return ResponseEntity.ok().body(userDetailResponse);
    }



    @Operation(summary = "사용자 정보 수정", description = "로그인한 사용자의 정보를 일부 수정합니다.")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @PatchMapping("")
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserPatchRequest userPatchRequest) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        userService.updateUser(userNo, userPatchRequest);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다. 기존 비밀번호 검증 후 새 비밀번호를 저장합니다.")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        userService.changePassword(userNo, userChangePasswordRequest);
        return ResponseEntity.ok().build();
    }

}
