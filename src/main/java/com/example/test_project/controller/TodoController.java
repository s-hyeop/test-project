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

import com.example.test_project.config.exception.UnauthorizedException;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.service.TodoService;
import com.example.test_project.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * TO-DO 관련 REST API 컨트롤러
 * 
 * <>사용자의 TO-DO 항목을 관리하는 엔드포인트를 제공합니다.
 * 모든 엔드포인트는 USER 권한이 필요합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TodoController {

    private final TodoService todoService;
    private final RateLimitUtil rateLimitUtil;

    /**
     * TO-DO 목록을 페이징하여 조회합니다.
     * 
     * @param todoListRequest 페이징 요청 정보 (page, size)
     * @return TO-DO 목록과 페이징 정보를 포함한 응답
     */
    @GetMapping("")
    public ResponseEntity<TodoListResponse> getTodos(@Valid @ModelAttribute TodoListRequest todoListRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 목록 조회 요청 - userNo: {}, page: {}, size: {}", userNo, todoListRequest.getPage(), todoListRequest.getSize());

        TodoListResponse todoListResponse = todoService.getTodos(userNo, todoListRequest);
        log.debug("TO-DO 목록 조회 완료 - userNo: {}, 조회된 항목 수: {}", userNo, todoListResponse.getList().size());

        return ResponseEntity.ok().body(todoListResponse);
    }


    /**
     * 특정 TO-DO 항목의 상세 정보를 조회합니다.
     * 
     * @param todoId 조회할 TODO의 ID
     * @return TO-DO 상세 정보
     */
    @GetMapping("/{todoId}")
    public ResponseEntity<TodoDetailResponse> getTodo(@PathVariable String todoId) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 상세 조회 요청 - userNo: {}, todoId: {}", userNo, todoId);
        TodoDetailResponse todoDetailResponse = todoService.getTodo(userNo, todoId);
        log.debug("TO-DO 상세 조회 완료 - userNo: {}, todoId: {}", userNo, todoId);

        return ResponseEntity.ok().body(todoDetailResponse);
    }


    /**
     * 새로운 TO-DO 항목을 생성합니다.
     * 
     * <p>Rate limiting이 적용되어 있어 과도한 요청이 제한됩니다.</p>
     * 
     * @param todoCreateRequest TO-DO 생성 요청 정보
     * @param request HTTP 요청 객체 (rate limiting용)
     * @return 생성된 TODO의 ID를 포함한 응답
     */
    @PostMapping("")
    public ResponseEntity<TodoCreateResponse> createTodo(@Valid @RequestBody TodoCreateRequest todoCreateRequest, HttpServletRequest request) {
        rateLimitUtil.checkRateLimit(request);
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 생성 요청 - userNo: {}, title: {}", userNo, todoCreateRequest.getTitle());
        TodoCreateResponse todoCreateResponse = todoService.createTodo(userNo, todoCreateRequest);
        log.info("TO-DO 생성 완료 - userNo: {}, todoId: {}", userNo, todoCreateResponse.getTodoId());

        return ResponseEntity.ok().body(todoCreateResponse);
    }


    /**
     * TO-DO 항목을 전체 수정합니다.
     * 
     * <p>모든 필드를 포함한 전체 업데이트를 수행합니다.</p>
     * 
     * @param todoId 수정할 TODO의 ID
     * @param todoUpdateRequest TO-DO 수정 요청 정보
     * @return 204 No Content
     */
    @PutMapping("/{todoId}")
    public ResponseEntity<Void> updateTodo(@PathVariable String todoId, @Valid @RequestBody TodoUpdateRequest todoUpdateRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 수정 요청 - userNo: {}, todoId: {}", userNo, todoId);
        todoService.updateTodo(userNo, todoId, todoUpdateRequest);
        log.info("TO-DO 수정 완료 - userNo: {}, todoId: {}", userNo, todoId);

        return ResponseEntity.noContent().build();
    }


    /**
     * TO-DO 항목을 부분 수정합니다.
     * 
     * <p>완료 상태나 순서 등 특정 필드만 수정할 때 사용합니다.</p>
     * 
     * @param todoId 수정할 TODO의 ID
     * @param todoPatchRequest 부분 수정 요청 정보
     * @return 204 No Content
     */
    @PatchMapping("/{todoId}")
    public ResponseEntity<Void> patchTodo(@PathVariable String todoId, @Valid @RequestBody TodoPatchRequest todoPatchRequest) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 부분 수정 요청 - userNo: {}, todoId: {}, sequence: {}, completed: {}", userNo, todoId, todoPatchRequest.getSequence(), todoPatchRequest.getCompleted());
        todoService.patchTodo(userNo, todoId, todoPatchRequest);
        log.info("TO-DO 부분 수정 완료 - userNo: {}, todoId: {}", userNo, todoId);

        return ResponseEntity.noContent().build();
    }


    /**
     * TO-DO 항목을 삭제합니다.
     * 
     * @param todoId 삭제할 TODO의 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable String todoId) {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 삭제 요청 - userNo: {}, todoId: {}", userNo, todoId);
        todoService.deleteTodo(userNo, todoId);
        log.info("TO-DO 삭제 완료 - userNo: {}, todoId: {}", userNo, todoId);

        return ResponseEntity.noContent().build();
    }


    /**
     * 사용자의 TO-DO 통계 정보를 조회합니다.
     * 
     * <p>전체 TO-DO 수, 완료된 TO-DO 수, 오늘 완료한 TO-DO 수 등의 통계를 제공합니다.</p>
     * 
     * @return TO-DO 통계 정보
     */
    @GetMapping("/statistics")
    public ResponseEntity<TodoStatisticsResponse> getTodoStatistics() {
        Integer userNo = AuthUtil.getCurrentUserNo();
        if (userNo == null) {
            throw new UnauthorizedException("인증 토큰이 잘못되었습니다.");
        }

        log.info("TO-DO 통계 조회 요청 - userNo: {}", userNo);
        TodoStatisticsResponse todoStatisticsResponse = todoService.getTodoStatistics(userNo);
        log.debug("TO-DO 통계 조회 완료 - userNo: {}, total: {}, completed: {}, todayCompleted: {}",
                userNo, todoStatisticsResponse.getTotalCount(),
                todoStatisticsResponse.getCompletedCount(),
                todoStatisticsResponse.getTodayCompletedCount());

        return ResponseEntity.ok().body(todoStatisticsResponse);
    }

}
