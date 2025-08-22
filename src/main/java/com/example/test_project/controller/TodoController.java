package com.example.test_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.TodoService;
import com.example.test_project.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TodoController {

    private final TodoService todoService;
    private final RateLimitUtil rateLimitUtil;



    @GetMapping("")
    public ResponseEntity<TodoListResponse> getTodos(@Valid @ModelAttribute TodoListRequest todoListRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();

        TodoListResponse todoListResponse = todoService.getTodos(userNo, todoListRequest);
        return ResponseEntity.ok().body(todoListResponse);
    }



    @GetMapping("/{todoId}")
    public ResponseEntity<TodoDetailResponse> getTodo(@PathVariable String todoId) {
        Integer userNo = AuthUtil.getCurrentUserNo();

        TodoDetailResponse todoDetailResponse = todoService.getTodo(userNo, todoId);
        return ResponseEntity.ok().body(todoDetailResponse);
    }



    @PostMapping("")
    public ResponseEntity<TodoCreateResponse> createTodo(@Valid @RequestBody TodoCreateRequest todoCreateRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        Integer userNo = AuthUtil.getCurrentUserNo();

        TodoCreateResponse todoCreateResponse = todoService.createTodo(userNo, todoCreateRequest);
        return ResponseEntity.ok().body(todoCreateResponse);
    }



    @PutMapping("/{todoId}")
    public ResponseEntity<Void> updateTodo(@PathVariable String todoId, @Valid @RequestBody TodoUpdateRequest todoUpdateRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();

        todoService.updateTodo(userNo, todoId, todoUpdateRequest);
        return ResponseEntity.noContent().build();
    }



    @PatchMapping("/{todoId}")
    public ResponseEntity<Void> patchTodo(@PathVariable String todoId, @Valid @RequestBody TodoPatchRequest todoPatchRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();

        todoService.patchTodo(userNo, todoId, todoPatchRequest);
        return ResponseEntity.noContent().build();
    }



    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable String todoId) {
        Integer userNo = AuthUtil.getCurrentUserNo();

        todoService.deleteTodo(userNo, todoId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/statistics")
    public ResponseEntity<TodoStatisticsResponse> getTodoStatistics() {
        Integer userNo = AuthUtil.getCurrentUserNo();

        TodoStatisticsResponse todoStatisticsResponse = todoService.getTodoStatistics(userNo);
        return ResponseEntity.ok().body(todoStatisticsResponse);
    }

}
