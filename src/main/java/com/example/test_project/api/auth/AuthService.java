package com.example.test_project.api.auth;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import com.example.test_project.api.auth.dto.request.EmailValidateRequest;
import com.example.test_project.api.auth.dto.request.LoginRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordCodeSendRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordCodeVerifyRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordRequest;
import com.example.test_project.api.auth.dto.request.SignupCodeSendRequest;
import com.example.test_project.api.auth.dto.request.SignupCodeVerifyRequest;
import com.example.test_project.api.auth.dto.request.SignupRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DSLContext dsl;

    public void validateEmail(EmailValidateRequest emailValidateRequest) {

    }

    public void login(LoginRequest loginRequest) {

    }

    public void sendSignupCode(SignupCodeSendRequest signupCodeSendRequest) {

    }

    public void verifySignupCode(SignupCodeVerifyRequest signupCodeVerifyRequest) {

    }

    public void signup(SignupRequest signupRequest) {

    }

    public void sendResetPasswordCode(ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {

    }

    public void verifyResetPasswordCode(ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {

    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {

    }

    public void getTokens(int userNo) {

    }

    public void refreshAccessToken(int userNo, String refreshToken) {

    }

    public void deleteToken(int userNo, String refreshToken) {

    }

}
