package com.example.test_project.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    // 비밀키
    private String jwtSecret;

    // 쿠키이름
    private String jwtRefreshTokenCookieName;

    // AT 만료(분)
    private int jwtAccessExpirationMinutes;

    // RT 만료(분, 30일)
    private int jwtRefreshExpirationMinutes;

    // AT 재발급 가능한 임계 시점
    private int jwtAccessTokenReissueThresholdMinutes;

    // redis 회원가입 인증 코드 TTL, 5분
    private int redisTtlSignup;

    // redis 비밀번호 찾기 인증 코드 TTL, 5분
    private int redisTtlResetPassword;

    // rate limit
    // 5회 허용
    private int redisRatelimitMaxRequests;
    // 10초 동안
    private int redisRatelimitWindowSeconds;
}
