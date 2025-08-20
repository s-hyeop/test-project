package com.example.test_project.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokensRepository {

    private final DSLContext dsl;
    
}
