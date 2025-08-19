package com.example.test_project.api.auth;

import org.springframework.stereotype.Service;

import com.example.test_project.api.auth.dto.request.EmailValidateRequest;
import com.example.test_project.api.auth.dto.request.LoginRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordCodeSendRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordCodeVerifyRequest;
import com.example.test_project.api.auth.dto.request.ResetPasswordRequest;
import com.example.test_project.api.auth.dto.request.SignupCodeSendRequest;
import com.example.test_project.api.auth.dto.request.SignupCodeVerifyRequest;
import com.example.test_project.api.auth.dto.request.SignupRequest;
import com.example.test_project.api.auth.dto.response.AccessTokenResponse;
import com.example.test_project.api.auth.dto.response.RefreshTokenDetailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;

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
