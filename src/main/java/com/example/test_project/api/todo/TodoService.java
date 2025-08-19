package com.example.test_project.api.todo;

import org.springframework.stereotype.Service;

import com.example.test_project.api.todo.dto.request.TodoCreateRequest;
import com.example.test_project.api.todo.dto.request.TodoListRequest;
import com.example.test_project.api.todo.dto.request.TodoPatchRequest;
import com.example.test_project.api.todo.dto.request.TodoUpdateRequest;
import com.example.test_project.api.todo.dto.response.TodoCreateResponse;
import com.example.test_project.api.todo.dto.response.TodoDetailResponse;
import com.example.test_project.api.todo.dto.response.TodoListResponse;
import com.example.test_project.api.todo.dto.response.TodoStatisticsResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoListResponse getTodos(int userNo, TodoListRequest todoListRequest) {
        return TodoListResponse.builder().build();
    }

    public TodoDetailResponse getTodo(int userNo, String todoId) {
        return TodoDetailResponse.builder().build();
    }

    public TodoCreateResponse createTodo(int userNo, TodoCreateRequest todoCreateRequest) {
        return TodoCreateResponse.builder().build();
    }

    public void updateTodo(int userNo, String todoId, TodoUpdateRequest todoUpdateRequest) {

    }

    public void patchTodo(int userNo, String todoId, TodoPatchRequest todoPatchRequest) {

    }

    public void deleteTodo(int userNo, String todoId) {

    }

    public TodoStatisticsResponse getTodoStatistics(int userNo, String todoId) {
        return TodoStatisticsResponse.builder().build();
    }

}
