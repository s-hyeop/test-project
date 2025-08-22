package com.example.test_project.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.test_project.config.exception.TooManyRequestsException;
import com.example.test_project.config.properties.AppProperties;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API 요청 속도 제한 유틸리티 클래스
 * 
 * <p>Redis를 사용하여 IP 기반의 요청 속도 제한을 구현합니다.
 * 지정된 시간 윈도우 내에서 허용된 최대 요청 수를 초과하면 예외를 발생시킵니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final List<String> IP_HEADERS = Arrays.asList(
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP"
    );

    /**
     * 요청에 대한 속도 제한을 검사합니다.
     * 
     * <p>클라이언트 IP를 기준으로 요청 횟수를 추적하고,
     * 제한을 초과하면 {@link TooManyRequestsException}을 발생시킵니다.</p>
     * 
     * @param request HTTP 요청 객체
     * @throws TooManyRequestsException 요청 속도 제한 초과 시
     * @throws IllegalArgumentException request가 null인 경우
     */
    public void checkRateLimit(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        String clientIp = resolveClientIp(request);
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + clientIp;

        Long requestCount = incrementRequestCount(rateLimitKey);
        
        if (requestCount == 1L) {
            setExpirationTime(rateLimitKey);
        }

        if (requestCount > appProperties.getRedisRatelimitMaxRequests()) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new TooManyRequestsException("요청 횟수 제한을 초과했습니다.");
        }
    }

    /**
     * 요청 카운트를 증가시킵니다.
     */
    private Long incrementRequestCount(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    /**
     * 키의 만료 시간을 설정합니다.
     */
    private void setExpirationTime(String key) {
        Duration window = Duration.ofSeconds(appProperties.getRedisRatelimitWindowSeconds());
        redisTemplate.expire(key, window);
    }

    /**
     * HTTP 요청에서 실제 클라이언트 IP를 추출합니다.
     * 
     * <p>프록시나 로드 밸런서를 거친 요청의 경우 X-Forwarded-For 등의 헤더를 확인하여
     * 실제 클라이언트 IP를 추출합니다.</p>
     * 
     * @param request HTTP 요청 객체
     * @return 클라이언트 IP 주소
     */
    private String resolveClientIp(HttpServletRequest request) {
        // 여러 프록시 헤더 확인
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // 여러 IP가 있는 경우 첫 번째 IP 반환
                return ip.contains(",") ? ip.split(",")[0].trim() : ip.trim();
            }
        }
        
        // 헤더에서 찾지 못한 경우 기본 remote address 사용
        return request.getRemoteAddr();
    }
    
    /**
     * IP 주소 유효성을 검사합니다.
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}