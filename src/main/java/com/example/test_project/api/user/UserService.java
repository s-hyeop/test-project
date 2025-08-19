package com.example.test_project.api.user;

import org.springframework.stereotype.Service;

import com.example.test_project.api.user.dto.response.UserDetailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDetailResponse getUserDetail(int userNo) {
        return UserDetailResponse.builder().build();
    }

    public void updateUser(int userNo) {

    }

    public void changePassword(int userNo) {

    }

}
