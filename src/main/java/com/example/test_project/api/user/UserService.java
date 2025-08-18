package com.example.test_project.api.user;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final DSLContext dsl;

    public void getUserDetail(int userNo) {

    }

    public void updateUser(int userNo) {

    }

    public void changePassword(int userNo) {

    }

}
