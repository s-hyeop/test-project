package com.example.test_project.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.jooq.tables.JUsers;
import com.example.jooq.tables.pojos.Users;
import com.example.test_project.dto.request.UserPatchRequest;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DSLContext dsl;
    private final JUsers USERS = JUsers.USERS;

    public Optional<Users> find(int userNo) {
        return dsl.selectFrom(USERS)
            .where(USERS.USER_NO.eq(userNo))
            .fetchOptionalInto(Users.class);
    }

    public int patch(int userNo, UserPatchRequest request) {
        Map<Field<?>, Object> updateMap = new HashMap<>();

        if (StringUtils.hasText(request.getUserName())) {
            updateMap.put(USERS.USER_NAME, request.getUserName());
        }

        if (updateMap.isEmpty()) return 0;

        return dsl.update(USERS)
                .set(updateMap)
                .where(USERS.USER_NO.eq(userNo))
                .execute();
    }

    public int patchPassword(int userNo, String password) {
        return dsl.update(USERS)
                .set(USERS.PASSWORD, password)
                .where(USERS.USER_NO.eq(userNo))
                .execute();
    }
}
