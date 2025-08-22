package com.example.test_project.repository;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.jooq.tables.JUsers;
import com.example.jooq.tables.pojos.Users;
import com.example.jooq.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UsersRepository {

    private final DSLContext dslContext;
    private final JUsers USERS = JUsers.USERS;

    /**
     * 사용자 번호로 사용자 정보를 조회합니다.
     * 
     * @param userNo 조회할 사용자 번호
     * @return 사용자 정보를 담은 Optional 객체, 존재하지 않을 경우 Optional.empty()
     */
    public Optional<Users> find(int userNo) {
        return dslContext.selectFrom(USERS)
                .where(USERS.USER_NO.eq(userNo))
                .fetchOptionalInto(Users.class);
    }


    /**
     * 사용자 이메일로 사용자 정보를 조회합니다.
     * 
     * @param email 조회할 사용자 이메일
     * @return 사용자 정보를 담은 Optional 객체, 존재하지 않을 경우 Optional.empty()
     */
    public Optional<Users> findByEmail(String email) {
        return dslContext.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptionalInto(Users.class);
    }


    /**
     * 새로운 사용자 정보를 저장합니다.
     * 
     * @param userPojo 저장할 사용자 정보 객체
     *                 <p>추가 필수 필드:</p>
     *                 <ul>
     *                   <li>email - 사용자 이메일</li>
     *                   <li>password - 암호화된 비밀번호</li>
     *                   <li>userName - 사용자 이름</li>
     *                   <li>role - 사용자 권한 (ADMIN, USER)</li>
     *                   <li>createdAt - 생성 일시</li>
     *                 </ul>
     * @return 생성된 사용자의 번호 (userNo)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public Integer save(Users userPojo) {
        return dslContext.insertInto(USERS)
                .set(USERS.EMAIL, userPojo.getEmail())
                .set(USERS.PASSWORD, userPojo.getPassword())
                .set(USERS.USER_NAME, userPojo.getUserName())
                .set(USERS.ROLE, userPojo.getRole())
                .set(USERS.CREATED_AT, userPojo.getCreatedAt())
                .returning(USERS.USER_NO)
                .fetchOne()
                .getUserNo();
    }


    /**
     * 기존 사용자 정보를 업데이트합니다.
     * 값이 null이거나 빈 문자열인 필드는 업데이트하지 않습니다.
     * 
     * @param userNo 업데이트할 사용자 번호
     * @param userPojo 업데이트할 사용자 정보 객체
     *                 <p>업데이트 가능한 필드:</p>
     *                 <ul>
     *                   <li>email - 사용자 이메일</li>
     *                   <li>password - 암호화된 비밀번호</li>
     *                   <li>userName - 사용자 이름</li>
     *                   <li>role - 사용자 권한 (ADMIN, USER 등)</li>
     *                   <li>deletedAt - 삭제 일시 (soft delete용)</li>
     *                   <li>lastLoginAt - 마지막 로그인 일시</li>
     *                 </ul>
     * @return 업데이트된 레코드 수 (0: 변경사항 없음, 1: 업데이트 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int update(int userNo, Users userPojo) {
        UsersRecord usersRecord = dslContext.newRecord(USERS);

        if (StringUtils.hasText(userPojo.getEmail())) {
            usersRecord.setEmail(userPojo.getEmail());
        }

        if (StringUtils.hasText(userPojo.getPassword())) {
            usersRecord.setPassword(userPojo.getPassword());
        }

        if (StringUtils.hasText(userPojo.getUserName())) {
            usersRecord.setUserName(userPojo.getUserName());
        }

        if (StringUtils.hasText(userPojo.getRole())) {
            usersRecord.setRole(userPojo.getRole());
        }

        if (userPojo.getDeletedAt() != null) {
            usersRecord.setDeletedAt(userPojo.getDeletedAt());
        }

        if (userPojo.getLastLoginAt() != null) {
            usersRecord.setLastLoginAt(userPojo.getLastLoginAt());
        }

        if (!usersRecord.changed()) {
            return 0;
        }

        return dslContext.update(USERS)
                .set(usersRecord)
                .where(USERS.USER_NO.eq(userNo))
                .execute();
    }


    /**
     * 사용자 정보를 삭제합니다.
     * 
     * @param userNo 삭제할 사용자 번호
     * @return 삭제된 레코드 수 (0: 해당 사용자 없음, 1: 삭제 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int delete(int userNo) {
        return dslContext.deleteFrom(USERS)
                .where(USERS.USER_NO.eq(userNo))
                .execute();
    }

}
