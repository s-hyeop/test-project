package com.example.test_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.UserService;
import com.example.test_project.util.AuthUtil;
import com.example.test_project.util.RateLimitUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;
    private final RateLimitUtil rateLimitUtil;



    @GetMapping("")
    public ResponseEntity<UserDetailResponse> getUserDetail() {
        Integer userNo = AuthUtil.getCurrentUserNo();

        UserDetailResponse userDetailResponse = userService.getUserDetail(userNo);
        return ResponseEntity.ok().body(userDetailResponse);
    }



    @PatchMapping("")
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserPatchRequest userPatchRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();

        userService.updateUser(userNo, userPatchRequest);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        Integer userNo = AuthUtil.getCurrentUserNo();

        userService.changePassword(userNo, userChangePasswordRequest);
        return ResponseEntity.noContent().build();
    }

}
