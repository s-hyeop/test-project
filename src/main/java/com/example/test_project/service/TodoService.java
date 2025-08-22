package com.example.test_project.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.tables.pojos.Todos;
import com.example.test_project.config.exception.*;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.repository.TodosRepository;
import com.example.test_project.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodosRepository todosRepository;


    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public TodoDetailResponse getTodo(int userNo, String todoId) {
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new NotFoundException("TODO를 찾을 수 없습니다.")
        );

        if (todoPojo.getUserNo() != userNo) {
            throw new ForbiddenException("TODO 조회 권한이 없습니다.");
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

    @Transactional
    public TodoCreateResponse createTodo(int userNo, TodoCreateRequest todoCreateRequest) {
        Todos todoPojo = new Todos();
        String todoId = UuidUtil.generateUuidV7(); // UUID v7

        todoPojo.setTodoId(todoId);
        todoPojo.setUserNo(userNo);
        todoPojo.setTitle(todoCreateRequest.getTitle());
        todoPojo.setContent(todoCreateRequest.getContent());
        todoPojo.setColor(todoCreateRequest.getColor());
        todoPojo.setDueAt(todoCreateRequest.getDueAt());
        todoPojo.setCreatedAt(LocalDateTime.now());

        if (todosRepository.save(todoPojo) == null) {
            throw new InternalServerException("TODO 생성에 실패했습니다.");
        }

        return TodoCreateResponse.builder()
            .todoId(todoId)
            .build();
    }

    @Transactional
    public void updateTodo(int userNo, String todoId, TodoUpdateRequest todoUpdateRequest) {
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new NotFoundException("TODO를 찾을 수 없습니다.")
        );

        if (todoPojo.getUserNo() != userNo) {
            throw new ForbiddenException("TODO 수정 권한이 없습니다.");
        }

        Todos updateTodoPojo = new Todos();
        updateTodoPojo.setTitle(todoUpdateRequest.getTitle());
        updateTodoPojo.setContent(todoUpdateRequest.getContent());
        updateTodoPojo.setColor(todoUpdateRequest.getColor());
        updateTodoPojo.setDueAt(todoUpdateRequest.getDueAt());
        updateTodoPojo.setUpdatedAt(LocalDateTime.now());

        if (todosRepository.update(todoId, updateTodoPojo) == 0) {
            throw new InternalServerException("TODO 수정에 실패했습니다.");
        }
    }

    @Transactional
    public void patchTodo(int userNo, String todoId, TodoPatchRequest todoPatchRequest) {
        int reslutCount = 0;
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new NotFoundException("TODO를 찾을 수 없습니다.")
        );

        if (todoPojo.getUserNo() != userNo) {
            throw new ForbiddenException("TODO 수정 권한이 없습니다.");
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
            throw new InternalServerException("TODO 수정에 실패했습니다.");
        }
    }

    @Transactional
    public void deleteTodo(int userNo, String todoId) {
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() ->
            new NotFoundException("TODO를 찾을 수 없습니다.")
        );

        if (todoPojo.getUserNo() != userNo) {
            throw new ForbiddenException("TODO 삭제 권한이 없습니다.");
        }

        if (todosRepository.delete(todoId) == 0) {
            throw new InternalServerException("TODO 삭제에 실패했습니다.");
        }
    }

    @Transactional(readOnly = true)
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
