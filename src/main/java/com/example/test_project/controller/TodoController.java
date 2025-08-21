package com.example.test_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.test_project.dto.request.TodoCreateRequest;
import com.example.test_project.dto.request.TodoListRequest;
import com.example.test_project.dto.request.TodoPatchRequest;
import com.example.test_project.dto.request.TodoUpdateRequest;
import com.example.test_project.dto.response.TodoCreateResponse;
import com.example.test_project.dto.response.TodoDetailResponse;
import com.example.test_project.dto.response.TodoListResponse;
import com.example.test_project.dto.response.TodoStatisticsResponse;
import com.example.test_project.service.TodoService;
import com.example.test_project.util.AuthUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "DOTO API", description = "TODO CRUD 및 통계 API")
public class TodoController {

    private final TodoService todoService;



    @GetMapping("")
    @Operation(summary = "TODO 목록 조회", description = "사용자가 등록한 TODO 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 목록 조회됨")
    public ResponseEntity<TodoListResponse> getTodos(@Valid @RequestParam TodoListRequest todoListRequest) {
        Integer userNo = AuthUtil.currentUserNo();

        TodoListResponse todoListResponse = todoService.getTodos(userNo, todoListRequest);
        return ResponseEntity.ok().body(todoListResponse);
    }



    @GetMapping("/{todoId}")
    @Operation(summary = "TODO 상세 조회", description = "특정 TODO의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 상세 조회됨")
    public ResponseEntity<TodoDetailResponse> getTodo(@PathVariable String todoId) {
        Integer userNo = AuthUtil.currentUserNo();

        TodoDetailResponse todoDetailResponse = todoService.getTodo(userNo, todoId);
        return ResponseEntity.ok().body(todoDetailResponse);
    }



    @PostMapping("")
    @Operation(summary = "TODO 등록", description = "새로운 TODO을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 등록됨")
    public ResponseEntity<TodoCreateResponse> createTodo(@Valid @RequestBody TodoCreateRequest todoCreateRequest) {
        Integer userNo = AuthUtil.currentUserNo();

        TodoCreateResponse todoCreateResponse = todoService.createTodo(userNo, todoCreateRequest);
        return ResponseEntity.ok().body(todoCreateResponse);
    }



    @PutMapping("/{todoId}")
    @Operation(summary = "TODO 전체 수정", description = "특정 TODO의 모든 내용을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 수정됨")
    public ResponseEntity<Void> updateTodo(@PathVariable String todoId, @Valid @RequestBody TodoUpdateRequest todoUpdateRequest) {
        Integer userNo = AuthUtil.currentUserNo();

        todoService.updateTodo(userNo, todoId, todoUpdateRequest);
        return ResponseEntity.noContent().build();
    }



    @PatchMapping("/{todoId}")
    @Operation(summary = "TODO 부분 수정", description = "특정 TODO의 일부 내용을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 부분 수정됨")
    public ResponseEntity<Void> patchTodo(@PathVariable String todoId, @Valid @RequestBody TodoPatchRequest todoPatchRequest) {
        Integer userNo = AuthUtil.currentUserNo();

        todoService.patchTodo(userNo, todoId, todoPatchRequest);
        return ResponseEntity.noContent().build();
    }



    @DeleteMapping("/{todoId}")
    @Operation(summary = "TODO 삭제", description = "특정 TODO을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 삭제됨")
    public ResponseEntity<Void> deleteTodo(@PathVariable String todoId) {
        Integer userNo = AuthUtil.currentUserNo();

        todoService.deleteTodo(userNo, todoId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/statistics")
    @Operation(summary = "TODO 통계 조회", description = "사용자의 TODO 통계를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 통계 조회됨")
    public ResponseEntity<TodoStatisticsResponse> getTodoStatistics() {
        Integer userNo = AuthUtil.currentUserNo();

        TodoStatisticsResponse todoStatisticsResponse = todoService.getTodoStatistics(userNo);
        return ResponseEntity.ok().body(todoStatisticsResponse);
    }

}
