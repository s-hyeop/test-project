package com.example.test_project.service;

import org.springframework.stereotype.Service;

import com.example.jooq.tables.pojos.Users;
import com.example.test_project.dto.request.UserChangePasswordRequest;
import com.example.test_project.dto.request.UserPatchRequest;
import com.example.test_project.dto.response.UserDetailResponse;
import com.example.test_project.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    public UserDetailResponse getUserDetail(int userNo) {
        Users users = usersRepository.find(userNo)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없음.")); // TODO: 예외 후처리 필요

        return UserDetailResponse.builder()
            .email(users.getEmail())
            .userName(users.getUserName())
            .createdAt(users.getCreatedAt())
            .build();
        }

    public void updateUser(int userNo, UserPatchRequest userPatchRequest) {
        usersRepository.find(userNo)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없음.")); // TODO: 예외 후처리 필요

        Users userPojo = new Users();
        userPojo.setUserName(userPatchRequest.getUserName());

        if (usersRepository.update(userNo, userPojo) == 0) {
            new RuntimeException("회원 정보 수정에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

    public void changePassword(int userNo, UserChangePasswordRequest userChangePasswordRequest) {
        Users users = usersRepository.find(userNo)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없음.")); // TODO: 예외 후처리 필요

        // DOTO: Spring Security 작업 후 해싱 처리 및 비교 추가 필요
        if (userChangePasswordRequest.getPassword() != users.getPassword()) {
            new RuntimeException("비밀번호가 일치하지 않음."); // TODO: 예외 후처리 필요
        }
        String passwordHash = userChangePasswordRequest.getNewPassword(); // TODO: Spring Security 작업 후

        Users userPojo = new Users();
        userPojo.setPassword(passwordHash);

        if (usersRepository.update(userNo, userPojo) == 0) {
            new RuntimeException("비밀번호 변경에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

}
