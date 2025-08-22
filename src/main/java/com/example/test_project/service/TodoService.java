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
import lombok.extern.slf4j.Slf4j;


/**
 * TO-DO 관리 비즈니스 로직 서비스
 * 
 * <p>TO-DO 항목의 생성, 조회, 수정, 삭제 및 통계 정보 제공 등의 비즈니스 로직을 처리합니다.
 * 모든 TO-DO 작업은 사용자별로 격리되어 관리됩니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodosRepository todosRepository;

    /**
     * 사용자의 TO-DO 목록을 페이징하여 조회합니다.
     * 
     * <p>페이지 번호와 크기를 기준으로 TO-DO 목록을 조회하고,
     * 전체 개수 정보와 함께 반환합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param todoListRequest 페이징 요청 정보 (page, size)
     * @return TO-DO 목록과 페이징 정보
     */
    @Transactional(readOnly = true)
    public TodoListResponse getTodos(int userNo, TodoListRequest todoListRequest) {
        log.debug("TO-DO 목록 조회 시작 - userNo: {}, page: {}, size: {}, status: {}, searchType: {}, keyword: {}", 
                userNo,
                todoListRequest.getPage(),
                todoListRequest.getSize(),
                todoListRequest.getStatus(),
                todoListRequest.getSearchType(),
                todoListRequest.getKeyword()
        );

        // 페이징된 TO-DO 목록 조회 (page는 0부터 시작하므로 -1)
        List<Todos> todosPojo = todosRepository.findPageByUserNo(userNo, 
                todoListRequest.getPage() - 1,
                todoListRequest.getSize(),
                todoListRequest.getStatus(),
                todoListRequest.getSearchType(),
                todoListRequest.getKeyword()
        );

        // DTO 변환
        List<TodoDetailResponse> dtoList = todosPojo.stream()
            .map(row -> TodoDetailResponse.builder()
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

        // 개수 조회
        int totalCount = todosRepository.countPageByUserNo(
            userNo,
            todoListRequest.getStatus(),
            todoListRequest.getSearchType(),
            todoListRequest.getKeyword()
        );

        log.debug("TO-DO 목록 조회 완료 - userNo: {}, 조회된 항목 수: {}, 전체 개수: {}", userNo, dtoList.size(), totalCount);

        return TodoListResponse.builder()
                .page(todoListRequest.getPage())
                .size(todoListRequest.getSize())
                .totalCount(totalCount)
                .list(dtoList)
                .build();
    }


    /**
     * 특정 TO-DO 항목의 상세 정보를 조회합니다.
     * 
     * <p>TO-DO ID로 항목을 조회하며, 본인의 TODO만 조회 가능합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param todoId TO-DO ID
     * @return TO-DO 상세 정보
     * @throws NotFoundException TODO를 찾을 수 없는 경우
     * @throws ForbiddenException TO-DO 조회 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public TodoDetailResponse getTodo(int userNo, String todoId) {
        log.debug("TO-DO 상세 조회 시작 - userNo: {}, todoId: {}", userNo, todoId);

        // TO-DO 조회
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() -> {
            log.warn("TO-DO 조회 실패 - TODO를 찾을 수 없음 - todoId: {}", todoId);
            return new NotFoundException("TODO를 찾을 수 없습니다.");
        });

        // 권한 확인
        if (todoPojo.getUserNo() != userNo) {
            log.warn("TO-DO 조회 실패 - 권한 없음 - userNo: {}, todoUserNo: {}, todoId: {}", userNo, todoPojo.getUserNo(), todoId);
            throw new ForbiddenException("TO-DO 조회 권한이 없습니다.");
        }

        log.debug("TO-DO 상세 조회 완료 - userNo: {}, todoId: {}, title: {}", userNo, todoId, todoPojo.getTitle());

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


    /**
     * 새로운 TO-DO 항목을 생성합니다.
     * 
     * <p>UUID v7을 사용하여 고유 ID를 생성하고 TODO를 저장합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param todoCreateRequest TO-DO 생성 요청 정보
     * @return 생성된 TODO의 ID
     * @throws InternalServerException TO-DO 생성에 실패한 경우
     */
    @Transactional
    public TodoCreateResponse createTodo(int userNo, TodoCreateRequest todoCreateRequest) {
        log.debug("TO-DO 생성 시작 - userNo: {}, title: {}", userNo, todoCreateRequest.getTitle());

        // UUID v7 생성
        String todoId = UuidUtil.generateUuidV7();
        log.debug("TO-DO ID 생성 완료 - todoId: {}", todoId);

        // TO-DO 엔티티 생성
        Todos todoPojo = new Todos();
        todoPojo.setTodoId(todoId);
        todoPojo.setUserNo(userNo);
        todoPojo.setTitle(todoCreateRequest.getTitle());
        todoPojo.setContent(todoCreateRequest.getContent());
        todoPojo.setColor(todoCreateRequest.getColor());
        todoPojo.setDueAt(todoCreateRequest.getDueAt());
        todoPojo.setCreatedAt(LocalDateTime.now());

        // TO-DO 저장
        if (todosRepository.save(todoPojo) == null) {
            log.error("TO-DO 생성 실패 - userNo: {}, title: {}", userNo, todoCreateRequest.getTitle());
            throw new InternalServerException("TO-DO 생성에 실패했습니다.");
        }

        log.info("TO-DO 생성 성공 - userNo: {}, todoId: {}, title: {}", userNo, todoId, todoCreateRequest.getTitle());

        return TodoCreateResponse.builder()
                .todoId(todoId)
                .build();
    }


    /**
     * TO-DO 항목을 전체 수정합니다.
     * 
     * <p>제목, 내용, 색상, 마감일 등 모든 필드를 수정합니다.
     * 본인의 TODO만 수정 가능합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param todoId 수정할 TO-DO ID
     * @param todoUpdateRequest TO-DO 수정 요청 정보
     * @throws NotFoundException TODO를 찾을 수 없는 경우
     * @throws ForbiddenException TO-DO 수정 권한이 없는 경우
     * @throws InternalServerException TO-DO 수정에 실패한 경우
     */
    @Transactional
    public void updateTodo(int userNo, String todoId, TodoUpdateRequest todoUpdateRequest) {
        log.debug("TO-DO 수정 시작 - userNo: {}, todoId: {}, title: {}", userNo, todoId, todoUpdateRequest.getTitle());

        // TO-DO 조회
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() -> {
            log.warn("TO-DO 수정 실패 - TODO를 찾을 수 없음 - todoId: {}", todoId);
            return new NotFoundException("TODO를 찾을 수 없습니다.");
        });

        // 권한 확인
        if (todoPojo.getUserNo() != userNo) {
            log.warn("TO-DO 수정 실패 - 권한 없음 - userNo: {}, todoUserNo: {}, todoId: {}", userNo, todoPojo.getUserNo(), todoId);
            throw new ForbiddenException("TO-DO 수정 권한이 없습니다.");
        }

        // 수정할 데이터 설정
        Todos updateTodoPojo = new Todos();
        updateTodoPojo.setTitle(todoUpdateRequest.getTitle());
        updateTodoPojo.setContent(todoUpdateRequest.getContent());
        updateTodoPojo.setColor(todoUpdateRequest.getColor());
        updateTodoPojo.setDueAt(todoUpdateRequest.getDueAt());
        updateTodoPojo.setUpdatedAt(LocalDateTime.now());

        // TO-DO 수정
        if (todosRepository.update(todoId, updateTodoPojo) == 0) {
            log.error("TO-DO 수정 실패 - todoId: {}", todoId);
            throw new InternalServerException("TO-DO 수정에 실패했습니다.");
        }

        log.info("TO-DO 수정 성공 - userNo: {}, todoId: {}", userNo, todoId);
    }


    /**
     * TO-DO 항목을 부분 수정합니다.
     * 
     * <p>순서(sequence) 또는 완료 상태(completed)만 선택적으로 수정합니다.
     * 본인의 TODO만 수정 가능합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param todoId 수정할 TO-DO ID
     * @param todoPatchRequest 부분 수정 요청 정보
     * @throws NotFoundException TODO를 찾을 수 없는 경우
     * @throws ForbiddenException TO-DO 수정 권한이 없는 경우
     * @throws InternalServerException TO-DO 수정에 실패한 경우
     */
    @Transactional
    public void patchTodo(int userNo, String todoId, TodoPatchRequest todoPatchRequest) {
        log.debug("TO-DO 부분 수정 시작 - userNo: {}, todoId: {}, sequence: {}, completed: {}",
                userNo, todoId, todoPatchRequest.getSequence(), todoPatchRequest.getCompleted());

        int resultCount = 0;

        // TO-DO 조회
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() -> {
            log.warn("TO-DO 부분 수정 실패 - TODO를 찾을 수 없음 - todoId: {}", todoId);
            return new NotFoundException("TODO를 찾을 수 없습니다.");
        });

        // 권한 확인
        if (todoPojo.getUserNo() != userNo) {
            log.warn("TO-DO 부분 수정 실패 - 권한 없음 - userNo: {}, todoUserNo: {}, todoId: {}",  userNo, todoPojo.getUserNo(), todoId);
            throw new ForbiddenException("TO-DO 수정 권한이 없습니다.");
        }

        // 순서 수정
        if (todoPatchRequest.getSequence() != null) {
            resultCount += todosRepository.updateSequence(todoId, todoPatchRequest.getSequence());
            log.debug("TO-DO 순서 수정 - todoId: {}, sequence: {}", todoId, todoPatchRequest.getSequence());
        }

        // 완료 상태 수정
        if (todoPatchRequest.getCompleted() != null) {
            LocalDateTime completedAt = todoPatchRequest.getCompleted() ? LocalDateTime.now() : null;
            resultCount += todosRepository.updateCompletedAt(todoId, completedAt);
            log.debug("TO-DO 완료 상태 수정 - todoId: {}, completed: {}", todoId, todoPatchRequest.getCompleted());
        }

        // 수정 결과 확인
        if (resultCount == 0) {
            log.error("TO-DO 부분 수정 실패 - 수정된 항목 없음 - todoId: {}", todoId);
            throw new InternalServerException("TO-DO 수정에 실패했습니다.");
        }

        log.info("TO-DO 부분 수정 성공 - userNo: {}, todoId: {}, 수정된 항목 수: {}",  userNo, todoId, resultCount);
    }


    /**
     * TO-DO 항목을 삭제합니다.
     * 
     * <p>본인의 TODO만 삭제 가능합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param todoId 삭제할 TO-DO ID
     * @throws NotFoundException TODO를 찾을 수 없는 경우
     * @throws ForbiddenException TO-DO 삭제 권한이 없는 경우
     * @throws InternalServerException TO-DO 삭제에 실패한 경우
     */
    @Transactional
    public void deleteTodo(int userNo, String todoId) {
        log.debug("TO-DO 삭제 시작 - userNo: {}, todoId: {}", userNo, todoId);

        // TO-DO 조회
        Todos todoPojo = todosRepository.find(todoId).orElseThrow(() -> {
            log.warn("TO-DO 삭제 실패 - TODO를 찾을 수 없음 - todoId: {}", todoId);
            return new NotFoundException("TODO를 찾을 수 없습니다.");
        });

        // 권한 확인
        if (todoPojo.getUserNo() != userNo) {
            log.warn("TO-DO 삭제 실패 - 권한 없음 - userNo: {}, todoUserNo: {}, todoId: {}",  userNo, todoPojo.getUserNo(), todoId);
            throw new ForbiddenException("TO-DO 삭제 권한이 없습니다.");
        }

        // TO-DO 삭제
        if (todosRepository.delete(todoId) == 0) {
            log.error("TO-DO 삭제 실패 - todoId: {}", todoId);
            throw new InternalServerException("TO-DO 삭제에 실패했습니다.");
        }

        log.info("TO-DO 삭제 성공 - userNo: {}, todoId: {}", userNo, todoId);
    }


    /**
     * 사용자의 TO-DO 통계 정보를 조회합니다.
     * 
     * <p>전체 TO-DO 수, 완료된 TO-DO 수, 오늘 완료한 TO-DO 수를 조회합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @return TO-DO 통계 정보
     */
    @Transactional(readOnly = true)
    public TodoStatisticsResponse getTodoStatistics(int userNo) {
        log.debug("TO-DO 통계 조회 시작 - userNo: {}", userNo);

        // 통계 데이터 조회
        int totalCount = todosRepository.countByUserNo(userNo);
        int completedCount = todosRepository.countCompletedByUserNo(userNo);
        int todayCompletedCount = todosRepository.countTodayCompletedByUserNo(userNo);

        log.debug("TO-DO 통계 조회 완료 - userNo: {}, total: {}, completed: {}, todayCompleted: {}",
                userNo, totalCount, completedCount, todayCompletedCount);

        return TodoStatisticsResponse.builder()
                .totalCount(totalCount)
                .completedCount(completedCount)
                .todayCompletedCount(todayCompletedCount)
                .build();
    }

}
