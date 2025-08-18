package com.example.test_project.api.user;

import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void getUserDetail(int userNo) {

    }

    public void updateUser(int userNo) {

    }

    public void changePassword(int userNo) {

    }

}
