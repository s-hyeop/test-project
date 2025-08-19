package com.example.test_project.api.todo;

import org.springframework.http.ResponseEntity;
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

import com.example.test_project.api.todo.dto.request.TodoCreateRequest;
import com.example.test_project.api.todo.dto.request.TodoListRequest;
import com.example.test_project.api.todo.dto.request.TodoPatchRequest;
import com.example.test_project.api.todo.dto.request.TodoUpdateRequest;
import com.example.test_project.api.todo.dto.response.TodoCreateResponse;
import com.example.test_project.api.todo.dto.response.TodoDetailResponse;
import com.example.test_project.api.todo.dto.response.TodoListResponse;
import com.example.test_project.api.todo.dto.response.TodoStatisticsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/todos")
@Tag(name = "DOTO API", description = "TODO CRUD 및 통계 API")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;



    @Operation(summary = "TODO 목록 조회", description = "사용자가 등록한 TODO 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 목록 조회됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @GetMapping("")
    public ResponseEntity<TodoListResponse> getTodos(@Valid @RequestParam TodoListRequest todoListRequest) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        TodoListResponse todoListResponse = todoService.getTodos(userNo, todoListRequest);
        return ResponseEntity.ok().body(todoListResponse);
    }



    @Operation(summary = "TODO 상세 조회", description = "특정 TODO의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 상세 조회됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @GetMapping("/{todoId}")
    public ResponseEntity<TodoDetailResponse> getTodo(@PathVariable String todoId) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        TodoDetailResponse todoDetailResponse = todoService.getTodo(userNo, todoId);
        return ResponseEntity.ok().body(todoDetailResponse);
    }



    @Operation(summary = "TODO 등록", description = "새로운 TODO을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 등록됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @PostMapping("")
    public ResponseEntity<TodoCreateResponse> createTodo(@Valid @RequestBody TodoCreateRequest todoCreateRequest) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        TodoCreateResponse todoCreateResponse = todoService.createTodo(userNo, todoCreateRequest);
        return ResponseEntity.ok().body(todoCreateResponse);
    }



    @Operation(summary = "TODO 전체 수정", description = "특정 TODO의 모든 내용을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 수정됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @PutMapping("/{todoId}")
    public ResponseEntity<Void> updateTodo(@PathVariable String todoId, @Valid @RequestBody TodoUpdateRequest todoUpdateRequest) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        todoService.updateTodo(userNo, todoId, todoUpdateRequest);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "TODO 부분 수정", description = "특정 TODO의 일부 내용을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 부분 수정됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @PatchMapping("/{todoId}")
    public ResponseEntity<Void> patchTodo(@PathVariable String todoId, @Valid @RequestBody TodoPatchRequest todoPatchRequest) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        todoService.patchTodo(userNo, todoId, todoPatchRequest);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "TODO 삭제", description = "특정 TODO을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 TODO 삭제됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable String todoId) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        todoService.deleteTodo(userNo, todoId);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "TODO 통계 조회", description = "사용자의 TODO 통계를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 통계 조회됨")
    // @PreAuthorize("hasRole('USER')") // TODO: 권한 상태가 USER 이상인가?
    @GetMapping("/statistics")
    public ResponseEntity<TodoStatisticsResponse> getTodoStatistics(@PathVariable String todoId) {
        int userNo = 1; // TODO: JWT 토큰에서 userNo 추출

        TodoStatisticsResponse todoStatisticsResponse = todoService.getTodoStatistics(userNo, todoId);
        return ResponseEntity.ok().body(todoStatisticsResponse);
    }

}
