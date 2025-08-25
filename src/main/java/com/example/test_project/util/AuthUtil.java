package com.example.test_project.util;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.test_project.config.security.CustomUserDetails;


/**
 * 인증 관련 유틸리티 클래스
 * 
 * <p>사용자 인증 정보 추출 및 인증 코드 생성 기능을 제공합니다.</p>
 * <p>회원가입 인증 코드와 비밀번호 재설정 토큰을 Redis에 저장하고 검증하는 기능을 제공합니다.</p>
 */
public final class AuthUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SIX_DIGIT_MAX = 1_000_000;
    private static final String SIX_DIGIT_FORMAT = "%06d";

    /**
     * 유틸리티 클래스의 인스턴스화 방지
     */
    private AuthUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }


    /**
     * 6자리 숫자 인증 코드를 생성합니다.
     * 
     * <p>암호학적으로 안전한 난수 생성기를 사용하여 000000부터 999999까지의
     * 6자리 숫자 문자열을 생성합니다.</p>
     * 
     * @return 6자리 숫자 문자열 (예: "123456", "000001")
     */
    public static String generateSixDigitCode() {
        int number = SECURE_RANDOM.nextInt(SIX_DIGIT_MAX);
        return String.format(SIX_DIGIT_FORMAT, number);
    }


    /**
     * 현재 인증된 사용자의 번호를 반환합니다.
     * 
     * <p>Spring Security Context에서 현재 인증된 사용자 정보를 추출하여
     * 사용자 번호를 반환합니다.</p>
     * 
     * @return 인증된 사용자의 번호, 인증되지 않은 경우 {@code null}
     */
    public static Integer getCurrentUserNo() {
        return getCurrentAuthentication()
            .filter(auth -> auth.getPrincipal() instanceof CustomUserDetails)
            .map(auth -> (CustomUserDetails) auth.getPrincipal())
            .map(CustomUserDetails::getUserNo)
            .orElse(null);
    }


    /**
     * 현재 인증 정보를 Optional로 반환합니다.
     * 
     * @return 현재 인증 정보를 포함한 Optional
     */
    private static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

}
