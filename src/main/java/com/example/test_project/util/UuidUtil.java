package com.example.test_project.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;

/**
 * UUID 생성 유틸리티 클래스
 * 
 * <p>시간 기반 정렬 가능한 UUID v7을 생성하는 기능을 제공합니다.</p>
 */
public final class UuidUtil {

    private static final TimeBasedEpochRandomGenerator UUID_V7_GENERATOR = Generators.timeBasedEpochRandomGenerator();

    /**
     * 유틸리티 클래스의 인스턴스화 방지
     */
    private UuidUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }


    /**
     * UUID v7을 생성합니다.
     * 
     * <p>UUID v7은 Unix 타임스탬프 기반으로 생성되어 시간순 정렬이 가능하며,
     * 데이터베이스 인덱싱 성능이 우수합니다.</p>
     * 
     * <pre>
     * 사용 예:
     * {@code
     * String uuid = UuidUtil.generateUuidV7();
     * // 결과: "018e4d2a-7b12-7e80-8f9a-1234567890ab"
     * }
     * </pre>
     * 
     * @return UUID v7 문자열 (하이픈 포함 36자)
     */
    public static String generateUuidV7() {
        return UUID_V7_GENERATOR.generate().toString();
    }


    /**
     * UUID v7을 생성하여 하이픈을 제거한 형태로 반환합니다.
     * 
     * @return 하이픈이 제거된 UUID v7 문자열 (32자)
     */
    public static String generateUuidV7WithoutHyphens() {
        return UUID_V7_GENERATOR.generate().toString().replace("-", "");
    }

}
