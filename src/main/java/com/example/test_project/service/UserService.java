package com.example.test_project.service;

import org.springframework.stereotype.Service;

import com.example.jooq.tables.pojos.Users;
import com.example.test_project.dto.request.UserChangePasswordRequest;
import com.example.test_project.dto.request.UserPatchRequest;
import com.example.test_project.dto.response.UserDetailResponse;
import com.example.test_project.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDetailResponse getUserDetail(int userNo) {
        Users users = userRepository.find(userNo)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없음.")); // TODO: 예외 후처리 필요

        return UserDetailResponse.builder()
            .email(users.getEmail())
            .userName(users.getUserName())
            .createdAt(users.getCreatedAt())
            .build();
        }

    public void updateUser(int userNo, UserPatchRequest userPatchRequest) {
        userRepository.find(userNo)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없음.")); // TODO: 예외 후처리 필요

        if (userRepository.patch(userNo, userPatchRequest) == 0) {
            new RuntimeException("회원 정보 수정에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

    public void changePassword(int userNo, UserChangePasswordRequest userChangePasswordRequest) {
        userRepository.find(userNo)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없음.")); // TODO: 예외 후처리 필요

        String passwordHash = userChangePasswordRequest.getNewPassword(); // Spring Security 작업 후
        if (userRepository.patchPassword(userNo, passwordHash) == 0) {
            new RuntimeException("비밀번호 변경에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

    //     public List<UserDetailResponse> getUserDetails() {
    //     List<UserDetailRow> rows = userRepository.findAllDetails();
    //     return rows.stream()
    //                .map(r -> UserDetailResponse.builder()
    //                        .userName(r.userName())
    //                        .email(r.email())
    //                        .createdAt(r.createdAt())
    //                        .build())
    //                .toList();
    // }

}
