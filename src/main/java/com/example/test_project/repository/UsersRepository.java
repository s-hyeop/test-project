package com.example.test_project.repository;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.jooq.tables.JUsers;
import com.example.jooq.tables.pojos.Users;
import com.example.jooq.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UsersRepository {

    private final DSLContext dslContext;
    private final JUsers USERS = JUsers.USERS;

    public Optional<Users> find(int userNo) {
        return dslContext.selectFrom(USERS)
                .where(USERS.USER_NO.eq(userNo))
                .fetchOptionalInto(Users.class);
    }

    public int save(Users userPojo) {
        return dslContext.insertInto(USERS)
                .set(USERS.EMAIL, userPojo.getEmail())
                .set(USERS.PASSWORD, userPojo.getPassword())
                .set(USERS.USER_NAME, userPojo.getUserName())
                .set(USERS.ROLE, userPojo.getRole())
                .set(USERS.CREATED_AT, userPojo.getCreatedAt())
                .returning(USERS.USER_NO) 
                .fetchOne()
                .getUserNo();
    }

    public int update(int userNo, Users userPojo) {
        UsersRecord usersRecord = dslContext.newRecord(USERS);

        if (StringUtils.hasText(userPojo.getEmail())) {
            usersRecord.setEmail(userPojo.getEmail());
        }

        if (StringUtils.hasText(userPojo.getPassword())) {
            usersRecord.setPassword(userPojo.getPassword());
        }

        if (StringUtils.hasText(userPojo.getUserName())) {
            usersRecord.setUserName(userPojo.getUserName());
        }

        if (StringUtils.hasText(userPojo.getRole())) {
            usersRecord.setRole(userPojo.getRole());
        }

        if (userPojo.getDeletedAt() != null) {
            usersRecord.setDeletedAt(userPojo.getDeletedAt());
        }

        if (userPojo.getLastLoginAt() != null) {
            usersRecord.setLastLoginAt(userPojo.getLastLoginAt());
        }

        if (!usersRecord.changed()) {
            return 0;
        }

        return dslContext.update(USERS)
                .set(usersRecord)
                .where(USERS.USER_NO.eq(userNo))
                .execute();
    }

    public int delete(int userNo) {
        return dslContext.deleteFrom(USERS)
                .where(USERS.USER_NO.eq(userNo))
                .execute();
    }
}
