package com.example.test_project.repository;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.jooq.tables.JTokens;
import com.example.jooq.tables.pojos.Tokens;
import com.example.jooq.tables.records.TokensRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokensRepository {

    private final DSLContext dslContext;
    private final JTokens TOKENS = JTokens.TOKENS;

    /**
     * 토큰 번호로 토큰 정보를 조회합니다.
     * 
     * @param tokenNo 조회할 토큰 번호
     * @return 토큰 정보를 담은 Optional 객체, 존재하지 않을 경우 Optional.empty()
     */
    public Optional<Tokens> find(int tokenNo) {
        return dslContext.selectFrom(TOKENS)
                .where(TOKENS.TOKEN_NO.eq(tokenNo))
                .fetchOptionalInto(Tokens.class);
    }


    /**
     * 토큰으로 토큰 정보를 조회합니다.
     * 
     * @param tokenNo 조회할 토큰
     * @return 토큰 정보를 담은 Optional 객체, 존재하지 않을 경우 Optional.empty()
     */
    public Optional<Tokens> findByRefreshToken(String refreshToken) {
        return dslContext.selectFrom(TOKENS)
                .where(TOKENS.REFRESH_TOKEN.eq(refreshToken))
                .fetchOptionalInto(Tokens.class);
    }


    public List<Tokens> findAllActiveTokensByUserNo(int userNo) {
        return dslContext.selectFrom(TOKENS)
                .where(TOKENS.USER_NO.eq(userNo))
                .orderBy(TOKENS.CREATED_AT.desc())
                .fetchInto(Tokens.class);
    }

    /**
     * 새로운 인증 토큰 정보를 저장합니다.
     * 사용자 로그인 시 Refresh Token 발급 시 호출됩니다.
     * 
     * @param tokenPojo 저장할 토큰 정보 객체
     *                 <p>추가 필수 필드:</p>
     *                   <li>userNo: 사용자 번호</li>
     *                   <li>refreshToken: 리프레시 토큰 값</li>
     *                   <li>clientOs: 클라이언트 OS 정보</li>
     *                   <li>accessTokenExpiresAt: Access Token 만료 일시</li>
     *                   <li>refreshTokenExpiresAt: Refresh Token 만료 일시</li>
     *                   <li>createdAt: 생성 일시</li>
     * @return 생성된 토큰의 사용자 번호
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public Integer save(Tokens tokenPojo) {
        return dslContext.insertInto(TOKENS)
                .set(TOKENS.USER_NO, tokenPojo.getUserNo())
                .set(TOKENS.REFRESH_TOKEN, tokenPojo.getRefreshToken())
                .set(TOKENS.CLIENT_OS, tokenPojo.getClientOs())
                .set(TOKENS.ACCESS_TOKEN_EXPIRES_AT, tokenPojo.getAccessTokenExpiresAt())
                .set(TOKENS.REFRESH_TOKEN_EXPIRES_AT, tokenPojo.getRefreshTokenExpiresAt())
                .set(TOKENS.CREATED_AT, tokenPojo.getCreatedAt())
                .returning(TOKENS.TOKEN_NO)
                .fetchOne()
                .getTokenNo();
    }


    /**
     * 기존 토큰 정보를 업데이트합니다.
     * 주로 토큰 갱신(refresh) 시 사용됩니다.
     * 값이 null이거나 빈 문자열인 필드는 업데이트하지 않습니다.
     * 
     * @param tokenNo 업데이트할 토큰 번호
     * @param tokenPojo 업데이트할 토큰 정보 객체
     *                  <p>업데이트 가능한 필드:</p>
     *                  <ul>
     *                    <li>refreshToken - 새로운 리프레시 토큰 값</li>
     *                    <li>accessTokenExpiresAt - Access Token 만료 일시</li>
     *                    <li>refreshTokenExpiresAt - Refresh Token 만료 일시</li>
     *                  </ul>
     * @return 업데이트된 레코드 수 (0: 변경사항 없음, 1: 업데이트 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int update(int tokenNo, Tokens tokenPojo) {
        TokensRecord tokensRecord = dslContext.newRecord(TOKENS);

        if (StringUtils.hasText(tokenPojo.getRefreshToken())) {
            tokensRecord.setRefreshToken(tokenPojo.getRefreshToken());
        }

        if (tokenPojo.getAccessTokenExpiresAt() != null) {
            tokensRecord.setAccessTokenExpiresAt(tokenPojo.getAccessTokenExpiresAt());
        }

        if (tokenPojo.getRefreshTokenExpiresAt() != null) {
            tokensRecord.setRefreshTokenExpiresAt(tokenPojo.getRefreshTokenExpiresAt());
        }

        if (!tokensRecord.changed()) {
            return 0;
        }

        return dslContext.update(TOKENS)
                .set(tokensRecord)
                .where(TOKENS.TOKEN_NO.eq(tokenNo))
                .execute();
    }


    /**
     * 토큰 정보를 물리적으로 삭제합니다.
     * 로그아웃 시 또는 토큰 무효화 시 호출됩니다.
     * 
     * @param tokenNo 삭제할 토큰 번호
     * @return 삭제된 레코드 수 (0: 해당 토큰 없음, 1: 삭제 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int delete(int tokenNo) {
        return dslContext.deleteFrom(TOKENS)
                .where(TOKENS.TOKEN_NO.eq(tokenNo))
                .execute();
    }


    /**
     * refreshToken 토큰 정보를 물리적으로 삭제합니다.
     * 로그아웃 시 또는 토큰 무효화 시 호출됩니다.
     * 
     * @param refreshToken 삭제할 토큰
     * @return 삭제된 레코드 수 (0: 해당 토큰 없음, 1: 삭제 성공)
     * @throws org.jooq.exception.DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public int deleteByRefreshToken(String refreshToken) {
        return dslContext.deleteFrom(TOKENS)
                .where(TOKENS.REFRESH_TOKEN.eq(refreshToken))
                .execute();
    }

}
