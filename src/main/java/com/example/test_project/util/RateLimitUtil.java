package com.example.test_project.util;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.test_project.config.exception.TooManyRequestsException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_KEY = "rate_limit:";

    @Value("${ratelimit.max-requests}")
    private int maxRequests;

    @Value("${ratelimit.window-seconds}")
    private int windowSeconds;

    public void checkRateLimit(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        String key = RATE_LIMIT_KEY + ip;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            // 최초 요청이면 TTL 설정
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (count != null && count > maxRequests) {
            throw new TooManyRequestsException("요청 횟수 제한을 초과했습니다.");
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
