package com.example.test_project.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-minutes}")
    private int accessExpireMinutes;

    @Value("${jwt.refresh-expiration-minutes}")
    private int refreshExpireMinutes;

    private SecretKey secretKey;

    @Override
    public void afterPropertiesSet() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); 
    }

    public String generateAccessToken(int userNo, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email) // (9) sub=email
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpireMinutes, ChronoUnit.MINUTES)))
                .claim("userNo", userNo)
                .claim("role", role)
                .signWith(secretKey)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public int getUserNo(String token) {
        Object v = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload().get("userNo");
        return (v instanceof Number n) ? n.intValue() : Integer.parseInt(String.valueOf(v));
    }

    public String getRole(String token) {
        Object v = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload().get("role");
        return v == null ? null : String.valueOf(v);
    }
}
