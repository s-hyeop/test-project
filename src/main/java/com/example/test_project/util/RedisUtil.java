package com.example.test_project.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.test_project.config.properties.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 캐시 관리 유틸리티 클래스
 * 
 * <p>회원가입 인증 코드와 비밀번호 재설정 토큰을 Redis에 저장하고 검증하는 기능을 제공합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    private static final String SIGNUP_KEY_PREFIX = "signup:";
    private static final String RESET_KEY_PREFIX = "resetPassword:";

    // ==================== 회원가입 관련 메서드 ====================
    
    /**
     * 회원가입 인증 키를 Redis에 저장합니다.
     * 
     * @param email 사용자 이메일
     * @param value 인증 코드 또는 토큰
     * @throws IllegalArgumentException email 또는 value가 null이거나 빈 값인 경우
     */
    public void saveSignupKey(String email, String value) {
        validateParameters(email, value);
        String key = SIGNUP_KEY_PREFIX + email;
        
        redisTemplate.opsForValue().set(
            key, 
            value, 
            appProperties.getRedisTtlSignup(), 
            TimeUnit.SECONDS
        );
        
        log.debug("Signup key saved for email: {}", email);
    }

    /**
     * 회원가입 인증 키를 검증합니다.
     * 
     * @param email 사용자 이메일
     * @param value 검증할 인증 코드 또는 토큰
     * @return 인증 성공 시 {@code true}, 실패 시 {@code false}
     * @throws IllegalArgumentException email 또는 value가 null이거나 빈 값인 경우
     */
    public boolean verifySignupKey(String email, String value) {
        validateParameters(email, value);
        return verifyKey(SIGNUP_KEY_PREFIX + email, value);
    }

    /**
     * 회원가입 인증 키를 삭제합니다.
     * 
     * @param email 사용자 이메일
     * @throws IllegalArgumentException email이 null이거나 빈 값인 경우
     */
    public void deleteSignupKey(String email) {
        validateEmail(email);
        deleteKey(SIGNUP_KEY_PREFIX + email);
        log.debug("Signup key deleted for email: {}", email);
    }

    // ==================== 비밀번호 재설정 관련 메서드 ====================
    
    /**
     * 비밀번호 재설정 키를 Redis에 저장합니다.
     * 
     * @param email 사용자 이메일
     * @param value 재설정 토큰
     * @throws IllegalArgumentException email 또는 value가 null이거나 빈 값인 경우
     */
    public void saveResetPasswordKey(String email, String value) {
        validateParameters(email, value);
        String key = RESET_KEY_PREFIX + email;
        
        redisTemplate.opsForValue().set(
            key, 
            value, 
            appProperties.getRedisTtlResetPassword(), 
            TimeUnit.SECONDS
        );
        
        log.debug("Reset password key saved for email: {}", email);
    }

    /**
     * 비밀번호 재설정 키를 검증합니다.
     * 
     * @param email 사용자 이메일
     * @param value 검증할 재설정 토큰
     * @return 인증 성공 시 {@code true}, 실패 시 {@code false}
     * @throws IllegalArgumentException email 또는 value가 null이거나 빈 값인 경우
     */
    public boolean verifyResetPasswordKey(String email, String value) {
        validateParameters(email, value);
        return verifyKey(RESET_KEY_PREFIX + email, value);
    }

    /**
     * 비밀번호 재설정 키를 삭제합니다.
     * 
     * @param email 사용자 이메일
     * @throws IllegalArgumentException email이 null이거나 빈 값인 경우
     */
    public void deleteResetPasswordKey(String email) {
        validateEmail(email);
        deleteKey(RESET_KEY_PREFIX + email);
        log.debug("Reset password key deleted for email: {}", email);
    }

    // ==================== 공통 메서드 ====================
    
    /**
     * 키의 남은 TTL(Time To Live)을 조회합니다.
     * 
     * @param email 사용자 이메일
     * @param keyType 키 타입 ("signup" 또는 "reset")
     * @return TTL(초), 키가 없거나 만료된 경우 -1
     */
    public long getTimeToLive(String email, String keyType) {
        validateEmail(email);
        
        String prefix = "signup".equalsIgnoreCase(keyType) ? 
            SIGNUP_KEY_PREFIX : RESET_KEY_PREFIX;
        
        Long ttl = redisTemplate.getExpire(prefix + email, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
    
    /**
     * Redis에서 키 값을 검증합니다.
     */
    private boolean verifyKey(String key, String value) {
        String storedValue = redisTemplate.opsForValue().get(key);
        boolean isValid = storedValue != null && storedValue.equals(value);
        
        if (!isValid) {
            log.debug("Key verification failed for: {}", key);
        }
        
        return isValid;
    }
    
    /**
     * Redis에서 키를 삭제합니다.
     */
    private void deleteKey(String key) {
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("Key deleted: {}", key);
        }
    }
    
    /**
     * 이메일과 값 매개변수를 검증합니다.
     */
    private void validateParameters(String email, String value) {
        validateEmail(email);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }
    }
    
    /**
     * 이메일 매개변수를 검증합니다.
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }
}