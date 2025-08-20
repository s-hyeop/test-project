package com.example.test_project.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.jooq.tables.pojos.Todos;
import com.example.test_project.dto.request.TodoCreateRequest;
import com.example.test_project.dto.request.TodoListRequest;
import com.example.test_project.dto.request.TodoPatchRequest;
import com.example.test_project.dto.request.TodoUpdateRequest;
import com.example.test_project.dto.response.TodoCreateResponse;
import com.example.test_project.dto.response.TodoDetailResponse;
import com.example.test_project.dto.response.TodoListResponse;
import com.example.test_project.dto.response.TodoStatisticsResponse;
import com.example.test_project.repository.TodosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodosRepository todosRepository;


    public TodoListResponse getTodos(int userNo, TodoListRequest todoListRequest) {
        List<Todos> todosPojo = todosRepository.findPageByUserNo(userNo, todoListRequest.getPage()-1, todoListRequest.getSize());
        List<TodoDetailResponse> dtoList = todosPojo.stream()
            .map(row ->TodoDetailResponse.builder()
                .todoId(row.getTodoId())
                .title(row.getTitle())
                .content(row.getContent())
                .color(row.getColor())
                .sequence(row.getSequence())
                .dueAt(row.getDueAt())
                .completedAt(row.getCompletedAt())
                .createdAt(row.getCreatedAt())
                .updatedAt(row.getUpdatedAt())
                .build()
            ).toList();
        int totalCount = todosRepository.countByUserNo(userNo);

        return TodoListResponse
            .builder()
            .page(todoListRequest.getPage())
            .size(todoListRequest.getSize())
            .totalCount(totalCount)
            .list(dtoList)
            .build();
    }

    public TodoDetailResponse getTodo(int userNo, String todoId) {
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new RuntimeException("게시글을 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        if (todoPojo.getUserNo() != userNo) {
            new RuntimeException("사용자가 일치하지 않음"); // TODO: 예외 후처리 필요
        }

        return TodoDetailResponse.builder()
            .todoId(todoPojo.getTodoId())
            .title(todoPojo.getTitle())
            .content(todoPojo.getContent())
            .color(todoPojo.getColor())
            .sequence(todoPojo.getSequence())
            .dueAt(todoPojo.getDueAt())
            .completedAt(todoPojo.getCompletedAt())
            .createdAt(todoPojo.getCreatedAt())
            .updatedAt(todoPojo.getUpdatedAt())
            .build();
    }

    public TodoCreateResponse createTodo(int userNo, TodoCreateRequest todoCreateRequest) {
        Todos todoPojo = new Todos();
        String todoId = new String(); // TODO: UUID v7 추가 해야 함

        todoPojo.setTodoId(todoId);
        todoPojo.setUserNo(userNo);
        todoPojo.setTitle(todoCreateRequest.getTitle());
        todoPojo.setContent(todoCreateRequest.getContent());
        todoPojo.setColor(todoCreateRequest.getColor());
        todoPojo.setDueAt(todoCreateRequest.getDueAt());
        todoPojo.setCreatedAt(LocalDateTime.now());

        if (todosRepository.save(todoPojo) == 0) {
            new RuntimeException("TODO 추가에 실패함."); // TODO: 예외 후처리 필요
        }

        return TodoCreateResponse.builder()
            .todoId(todoId)
            .build();
    }

    public void updateTodo(int userNo, String todoId, TodoUpdateRequest todoUpdateRequest) {
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new RuntimeException("게시글을 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        if (todoPojo.getUserNo() != userNo) {
            new RuntimeException("사용자가 일치하지 않음"); // TODO: 예외 후처리 필요
        }

        Todos updateTodoPojo = new Todos();
        updateTodoPojo.setTitle(todoUpdateRequest.getTitle());
        updateTodoPojo.setContent(todoUpdateRequest.getContent());
        updateTodoPojo.setColor(todoUpdateRequest.getColor());
        updateTodoPojo.setDueAt(todoUpdateRequest.getDueAt());
        updateTodoPojo.setUpdatedAt(LocalDateTime.now());

        if (todosRepository.update(todoId, updateTodoPojo) == 0) {
            new RuntimeException("TODO 수정에 실패함."); // TODO: 예외 후처리 필요
        }
    }

    public void patchTodo(int userNo, String todoId, TodoPatchRequest todoPatchRequest) {
        int reslutCount = 0;
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new RuntimeException("게시글을 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        if (todoPojo.getUserNo() != userNo) {
            new RuntimeException("사용자가 일치하지 않음"); // TODO: 예외 후처리 필요
        }

        if (todoPatchRequest.getSequence() != null) {
            reslutCount += todosRepository.updateSequence(todoId, todoPatchRequest.getSequence());
        }

        if (todoPatchRequest.getCompleted() != null) {
            reslutCount += todosRepository.updateCompletedAt(todoId, 
                todoPatchRequest.getCompleted()
                ? LocalDateTime.now()
                : null
            );
        }

        if (reslutCount == 0) {
            new RuntimeException("TODO 수정에 실패함."); // TODO: 예외 후처리 필요
        }
    }

    public void deleteTodo(int userNo, String todoId) {
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new RuntimeException("게시글을 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        if (todoPojo.getUserNo() != userNo) {
            new RuntimeException("사용자가 일치하지 않음"); // TODO: 예외 후처리 필요
        }

        if (todosRepository.delete(todoId) == 0) {
            new RuntimeException("TODO 삭제에 실패함."); // TODO: 예외 후처리 필요
        }
    }

    public TodoStatisticsResponse getTodoStatistics(int userNo) {
        int totalCount = todosRepository.countByUserNo(userNo);
        int completedCount = todosRepository.countCompletedByUserNo(userNo);
        int todayCompletedCount = todosRepository.countTodayCompletedByUserNo(userNo);

        return TodoStatisticsResponse
            .builder()
            .totalCount(totalCount)
            .completedCount(completedCount)
            .todayCompletedCount(todayCompletedCount)
            .build();
    }

}
