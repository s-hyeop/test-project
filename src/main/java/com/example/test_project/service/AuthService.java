package com.example.test_project.service;

import org.springframework.stereotype.Service;

import com.example.test_project.dto.request.EmailValidateRequest;
import com.example.test_project.dto.request.LoginRequest;
import com.example.test_project.dto.request.ResetPasswordCodeSendRequest;
import com.example.test_project.dto.request.ResetPasswordCodeVerifyRequest;
import com.example.test_project.dto.request.ResetPasswordRequest;
import com.example.test_project.dto.request.SignupCodeSendRequest;
import com.example.test_project.dto.request.SignupCodeVerifyRequest;
import com.example.test_project.dto.request.SignupRequest;
import com.example.test_project.dto.response.AccessTokenResponse;
import com.example.test_project.dto.response.RefreshTokenDetailResponse;
import com.example.test_project.repository.TokensRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokensRepository tokensRepository;

    public void validateEmail(EmailValidateRequest emailValidateRequest) {

    }

    public AccessTokenResponse login(LoginRequest loginRequest) {
        return AccessTokenResponse.builder().build();
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

    public RefreshTokenDetailResponse getTokens(int userNo) {
        return RefreshTokenDetailResponse.builder().build();
    }

    public AccessTokenResponse refreshAccessToken(int userNo, String refreshToken) {
        return AccessTokenResponse.builder().build();
    }

    public void deleteToken(int userNo, String refreshToken) {

    }

}
