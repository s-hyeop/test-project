package com.example.test_project.api.todo;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.tables.daos.UsersDao;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TodoRepository {

    private final DSLContext dsl;
    private final UsersDao usersDao;
    
}
