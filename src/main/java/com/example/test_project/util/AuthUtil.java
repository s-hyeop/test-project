package com.example.test_project.util;

import java.security.SecureRandom;

import org.springframework.security.core.context.SecurityContextHolder;

import com.example.test_project.config.security.CustomUserDetails;

public class AuthUtil {
    private static final SecureRandom random = new SecureRandom();

    // 6자리 숫자 랜덤 문자열 생성
    public static String generateSixDigitCode() {
        int number = random.nextInt(1_000_000); // 0 ~ 999999
        return String.format("%06d", number);  // 6자리 zero padding
    }

    public static Integer currentUserNo() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof CustomUserDetails u) return u.getUserNo();
        return null;
    }
}
