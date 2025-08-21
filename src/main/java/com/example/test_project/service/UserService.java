package com.example.test_project.service;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UserDetailResponse getUserDetail(int userNo) {
        Users userPojo = usersRepository.find(userNo).orElseThrow(() ->
            new RuntimeException("회원을 찾을 수 없음.")  // TODO: 예외 후처리 필요
        );

        return UserDetailResponse.builder()
            .email(userPojo.getEmail())
            .userName(userPojo.getUserName())
            .createdAt(userPojo.getCreatedAt())
            .build();
    }

    public void updateUser(int userNo, UserPatchRequest userPatchRequest) {
        usersRepository.find(userNo).orElseThrow(() ->
            new RuntimeException("회원을 찾을 수 없음.")  // TODO: 예외 후처리 필요
        );

        Users updateUserPojo = new Users();
        updateUserPojo.setUserName(userPatchRequest.getUserName());

        if (usersRepository.update(userNo, updateUserPojo) == 0) {
            throw new RuntimeException("회원 정보 수정에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

    public void changePassword(int userNo, UserChangePasswordRequest userChangePasswordRequest) {
        Users userPojo = usersRepository.find(userNo).orElseThrow(() ->
            new RuntimeException("회원을 찾을 수 없음.")  // TODO: 예외 후처리 필요
        );
        

        if (!passwordEncoder.matches(userChangePasswordRequest.getPassword(), userPojo.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않음."); // TODO: 예외 후처리 필요
        }

        String hashPassword = passwordEncoder.encode(userChangePasswordRequest.getNewPassword());

        Users updateUserPojo = new Users();
        updateUserPojo.setPassword(hashPassword);

        if (usersRepository.update(userNo, updateUserPojo) == 0) {
            throw new RuntimeException("비밀번호 변경에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

}
