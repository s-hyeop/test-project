package com.example.test_project.api.user;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.tables.daos.UsersDao;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DSLContext dsl;
    private final UsersDao usersDao;
    
}
