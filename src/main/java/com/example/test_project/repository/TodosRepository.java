package com.example.test_project.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TodosRepository {

    private final DSLContext dsl;
    
}
