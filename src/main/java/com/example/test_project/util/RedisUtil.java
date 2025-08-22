package com.example.test_project.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.test_project.config.properties.AppProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    private static final String SIGNUP_PREFIX = "signup:";
    private static final String RESET_PREFIX  = "resetPassword:";

    // ---------------------- SIGNUP ----------------------
    public void saveSignupKey(String email, String value) {
        redisTemplate.opsForValue().set(SIGNUP_PREFIX + email, value, appProperties.getRedisTtlSignup(), TimeUnit.SECONDS);
    }

    public boolean verifySignupKey(String email, String value) {
        String stored = redisTemplate.opsForValue().get(SIGNUP_PREFIX + email);
        return stored != null && stored.equals(value);
    }

    public void deleteSignupKey(String email) {
        redisTemplate.delete(SIGNUP_PREFIX + email);
    }

    // ---------------------- RESET PASSWORD ----------------------
    public void saveResetPasswordKey(String email, String value) {
        redisTemplate.opsForValue().set(RESET_PREFIX + email, value, appProperties.getRedisTtlResetPassword(), TimeUnit.SECONDS);
    }

    public boolean verifyResetPasswordKey(String email, String value) {
        String stored = redisTemplate.opsForValue().get(RESET_PREFIX + email);
        return stored != null && stored.equals(value);
    }

    public void deleteResetPasswordKey(String email) {
        redisTemplate.delete(RESET_PREFIX + email);
    }
}
