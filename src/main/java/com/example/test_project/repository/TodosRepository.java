package com.example.test_project.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.jooq.tables.JTodos;
import com.example.jooq.tables.pojos.Todos;
import com.example.jooq.tables.records.TodosRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TodosRepository {

    private final DSLContext dslContext;
    private final JTodos TODOS = JTodos.TODOS;

    /**
     * Todo ID로 할 일 정보를 조회합니다.
     * 
     * @param todoId 조회할 Todo ID
     * @return Todo 정보를 담은 Optional 객체, 존재하지 않을 경우 Optional.empty()
     */
    public Optional<Todos> find(String todoId) {
        return dslContext.selectFrom(TODOS)
                .where(TODOS.TODO_ID.eq(todoId))
                .fetchOptionalInto(Todos.class);
    }


    /**
     * 특정 회원의 Todo 목록을 페이징하여 조회합니다.
     *
     * @param userNo 조회할 회원 번호
     * @param offset 시작 위치 (0 기반)
     * @param limit  조회할 데이터 개수
     * @return Todo 목록
     */
    public List<Todos> findPageByUserNo(int userNo, int offset, int limit) {
        return dslContext.selectFrom(TODOS)
                .where(TODOS.USER_NO.eq(userNo))
                .orderBy(TODOS.SEQUENCE.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(Todos.class);
    }


    /**
     * 특정 회원의 Todo 전체 개수를 조회합니다.
     *
     * @param userNo 조회할 회원 번호
     * @return 총 Todo 개수
     */
    public int countByUserNo(int userNo) {
        return dslContext.selectCount()
                .from(TODOS)
                .where(TODOS.USER_NO.eq(userNo))
                .fetchOne(0, int.class);
    }


    /**
     * 특정 회원의 완료된 Todo 개수를 조회합니다.
     * completedAt이 null이 아닌 Todo를 완료된 것으로 간주합니다.
     *
     * @param userNo 조회할 회원 번호
     * @return 완료된 Todo 개수
     */
    public int countCompletedByUserNo(int userNo) {
        return dslContext.selectCount()
                .from(TODOS)
                .where(TODOS.USER_NO.eq(userNo))
                .and(TODOS.COMPLETED_AT.isNotNull())
                .fetchOne(0, int.class);
    }


    /**
     * 특정 회원의 오늘 완료한 Todo 개수를 조회합니다.
     * completedAt이 오늘 날짜인 Todo를 조회합니다.
     *
     * @param userNo 조회할 회원 번호
     * @return 오늘 완료한 Todo 개수
     */
    public int countTodayCompletedByUserNo(int userNo) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        return dslContext.selectCount()
                .from(TODOS)
                .where(TODOS.USER_NO.eq(userNo))
                .and(TODOS.COMPLETED_AT.isNotNull())
                .and(TODOS.COMPLETED_AT.between(startOfDay, endOfDay))
                .fetchOne(0, int.class);
    }


    /**
     * 새로운 할 일을 저장합니다.
     * sequence가 null인 경우 해당 사용자의 최대 sequence + 1로 자동 설정됩니다.
     * 
     * @param todoPojo 저장할 Todo 정보 객체
     *                 <p>추가 필수 필드:</p>
     *                 <ul>
     *                   <li>todoId: Todo 고유 식별자 (UUIDv7)</li>
     *                   <li>userNo: 사용자 번호</li>
     *                   <li>title: 제목</li>
     *                   <li>content: 내용</li>
     *                   <li>color: 색상 코드</li>
     *                   <li>dueAt: 마감 일시</li>
     *                 </ul>
     * @return 생성된 Todo의 사용자 번호
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public String save(Todos todoPojo) {
        Integer sequence = todoPojo.getSequence();
        if (sequence == null) {
            sequence = getNextSequence(todoPojo.getUserNo());
        }

        return dslContext.insertInto(TODOS)
                .set(TODOS.TODO_ID, todoPojo.getTodoId())
                .set(TODOS.USER_NO, todoPojo.getUserNo())
                .set(TODOS.TITLE, todoPojo.getTitle())
                .set(TODOS.CONTENT, todoPojo.getContent())
                .set(TODOS.COLOR, todoPojo.getColor())
                .set(TODOS.SEQUENCE, sequence)
                .set(TODOS.DUE_AT, todoPojo.getDueAt())
                .set(TODOS.CREATED_AT, todoPojo.getCreatedAt())
                .returning(TODOS.TODO_ID) 
                .fetchOne()
                .getTodoId();
    }


    /**
     * 기존 할 일 정보를 업데이트합니다.
     * 값이 null이거나 빈 문자열인 필드는 업데이트하지 않습니다.
     * 
     * @param todoId 업데이트할 Todo ID
     * @param todoPojo 업데이트할 Todo 정보 객체
     *                 <p>업데이트 가능한 필드:</p>
     *                 <ul>
     *                   <li>title - 할 일 제목</li>
     *                   <li>content - 할 일 내용</li>
     *                   <li>color - 색상 코드</li>
     *                   <li>sequence - 정렬 순서 (사용자별 할 일 목록 정렬용)</li>
     *                   <li>dueAt - 마감 일시</li>
     *                   <li>updatedAt - 수정 일시</li>
     *                 </ul>
     * @return 업데이트된 레코드 수 (0: 변경사항 없음, 1: 업데이트 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int update(String todoId, Todos todoPojo) {
        TodosRecord todosRecord = dslContext.newRecord(TODOS);

        if (StringUtils.hasText(todoPojo.getTitle())) {
            todosRecord.setTitle(todoPojo.getTitle());
        }

        if (StringUtils.hasText(todoPojo.getContent())) {
            todosRecord.setContent(todoPojo.getContent());
        }

        if (StringUtils.hasText(todoPojo.getColor())) {
            todosRecord.setColor(todoPojo.getColor());
        }

        if (todoPojo.getSequence() != null) {
            todosRecord.setSequence(todoPojo.getSequence());
        }

        if (todoPojo.getDueAt() != null) {
            todosRecord.setDueAt(todoPojo.getDueAt());
        }

        if (todoPojo.getUpdatedAt() != null) {
            todosRecord.setUpdatedAt(todoPojo.getUpdatedAt());
        }

        if (!todosRecord.changed()) {
            return 0;
        }

        return dslContext.update(TODOS)
                .set(todosRecord)
                .where(TODOS.TODO_ID.eq(todoId))
                .execute();
    }


    /**
     * 기존 할 일 순서를 업데이트합니다.
     * 
     * @param todoId 업데이트할 Todo ID
     * @param sequence 업데이트할 순서
     * 
     * @return 업데이트된 레코드 수 (0: 변경사항 없음, 1: 업데이트 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int updateSequence(String todoId, int sequence) {
        return dslContext.update(TODOS)
                .set(TODOS.SEQUENCE, sequence)
                .where(TODOS.TODO_ID.eq(todoId))
                .execute();
    }


    /**
     * 기존 할 일 완료 날짜(=유무)를 업데이트합니다.
     * 
     * @param todoId 업데이트할 Todo ID
     * @param completedAt 업데이트 완료 날짜(=유무)
     * 
     * @return 업데이트된 레코드 수 (0: 변경사항 없음, 1: 업데이트 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int updateCompletedAt(String todoId, LocalDateTime completedAt) {
        return dslContext.update(TODOS)
                .set(TODOS.COMPLETED_AT, completedAt)
                .where(TODOS.TODO_ID.eq(todoId))
                .execute();
    }


    /**
     * 할 일을 물리적으로 삭제합니다.
     * 
     * @param todoId 삭제할 Todo ID
     * @return 삭제된 레코드 수 (0: 해당 Todo 없음, 1: 삭제 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int delete(String todoId) {
        return dslContext.deleteFrom(TODOS)
                .where(TODOS.TODO_ID.eq(todoId))
                .execute();
    }


    /**
     * 특정 사용자의 다음 시퀀스 번호를 가져옵니다.
     * 해당 사용자의 최대 시퀀스 값에 1을 더한 값을 반환합니다.
     * 
     * @param userNo 사용자 번호
     * @return 다음 시퀀스 번호 (기존 Todo가 없으면 1 반환)
     */
    private Integer getNextSequence(int userNo) {
        Integer maxSequence = dslContext
                .select(DSL.max(TODOS.SEQUENCE))
                .from(TODOS)
                .where(TODOS.USER_NO.eq(userNo))
                .fetchOneInto(Integer.class);
        
        return maxSequence != null ? maxSequence + 1 : 1;
    }
}
