package com.example.test_project.config.security.provider;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.example.test_project.config.properties.AppProperties;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements InitializingBean {

    private final AppProperties appProperties;

    private SecretKey secretKey;

    @Override
    public void afterPropertiesSet() {
        this.secretKey = Keys.hmacShaKeyFor(appProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8)); 
    }

    public String generateAccessToken(int userNo, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email) // (9) sub=email
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(appProperties.getJwtAccessExpirationMinutes(), ChronoUnit.MINUTES)))
                .claim("userNo", userNo)
                .claim("role", role)
                .signWith(secretKey)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public String getEmail(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public int getUserNo(String token) {
        Object v = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("userNo");

        return (v instanceof Number n) ? n.intValue() : Integer.parseInt(String.valueOf(v));
    }

    public String getRole(String token) {
        Object v = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role");

        return v == null ? null : String.valueOf(v);
    }

}
