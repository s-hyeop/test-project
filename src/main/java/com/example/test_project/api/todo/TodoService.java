package com.example.test_project.api.todo;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import com.example.test_project.api.todo.dto.request.TodoCreateRequest;
import com.example.test_project.api.todo.dto.request.TodoListRequest;
import com.example.test_project.api.todo.dto.request.TodoPatchRequest;
import com.example.test_project.api.todo.dto.request.TodoUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final DSLContext dsl;

    public void getTodos(int userNo, TodoListRequest todoListRequest) {

    }

    public void getTodo(int userNo, String todoId) {

    }

    public void createTodo(int userNo, TodoCreateRequest todoCreateRequest) {

    }

    public void updateTodo(int userNo, String todoId, TodoUpdateRequest todoUpdateRequest) {

    }

    public void patchTodo(int userNo, String todoId, TodoPatchRequest todoPatchRequest) {

    }

    public void deleteTodo(int userNo, String todoId) {

    }

    public void getTodoStatistics(int userNo, String todoId) {

    }

}
