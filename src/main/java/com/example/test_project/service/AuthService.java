package com.example.test_project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.jooq.tables.pojos.Tokens;
import com.example.jooq.tables.pojos.Users;
import com.example.test_project.dto.request.EmailRequest;
import com.example.test_project.dto.request.LoginRequest;
import com.example.test_project.dto.request.ResetPasswordCodeSendRequest;
import com.example.test_project.dto.request.ResetPasswordCodeVerifyRequest;
import com.example.test_project.dto.request.ResetPasswordRequest;
import com.example.test_project.dto.request.SignupCodeSendRequest;
import com.example.test_project.dto.request.SignupCodeVerifyRequest;
import com.example.test_project.dto.request.SignupRequest;
import com.example.test_project.dto.response.AccessTokenResponse;
import com.example.test_project.dto.response.EmailExistResponse;
import com.example.test_project.dto.response.RefreshTokenDetailResponse;
import com.example.test_project.repository.TokensRepository;
import com.example.test_project.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final TokensRepository tokensRepository;

    public EmailExistResponse existsByEmail(EmailRequest emailRequest) {
        // 계정이 없으면 true, 계정이 있으면 false
        boolean result = usersRepository.findByEmail(emailRequest.getEmail()).isPresent();

        return EmailExistResponse.builder()
            .exists(result)
            .build();
    }

    public AccessTokenResponse login(LoginRequest loginRequest) {
        // TODO: Spring Security Login

        // TODO: 토큰 발급

        // TODO: 토큰 DB 반영
        
        return AccessTokenResponse.builder().build();
    }

    public void sendSignupCode(SignupCodeSendRequest signupCodeSendRequest) {
        // 계정이 존재하지 않는지 확인
        if (!usersRepository.findByEmail(signupCodeSendRequest.getEmail()).isEmpty()) {
            throw new RuntimeException("이미 사용중인 이메일."); // TODO: 예외 후처리 필요
        }

        // TODO: 이메일 발송
        // TODO: redis 에 추가
    }

    public void verifySignupCode(SignupCodeVerifyRequest signupCodeVerifyRequest) {
        // TODO: redis 비교
    }

    public void signup(SignupRequest signupRequest) {
        // 계정이 존재하지 않는지 확인
        if (!usersRepository.findByEmail(signupRequest.getEmail()).isEmpty()) {
            throw new RuntimeException("이미 사용중인 이메일."); // TODO: 예외 후처리 필요
        }

        // TODO: redis 비교 -> 삭제
        // TODO: 계정 추가
    }

    public void sendResetPasswordCode(ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {
        // 계정이 존재하는지 확인
        usersRepository.findByEmail(resetPasswordCodeSendRequest.getEmail()).orElseThrow(() ->
            new RuntimeException("존재하지 않는 이메일.")  // TODO: 예외 후처리 필요
        );

        // TODO: 이메일 발송
        // TODO: redis에 추가
    }

    public void verifyResetPasswordCode(ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {
        // TODO: redis 비교
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // 계정이 존재하는지 확인
        Users userPojo = usersRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(() ->
            new RuntimeException("존재하지 않는 이메일.")  // TODO: 예외 후처리 필요
        );

        // TODO: redos 비교 -> 삭제

        // 새로운 비밀번호 해싱
        String passwordHash = resetPasswordRequest.getPassword(); // TODO: Spring Security 작업 후

        // 변경할 비밀번호 pojo에 반영
        Users updateUserPojo = new Users();
        updateUserPojo.setPassword(passwordHash);

        // 업데이트 진행
        if (usersRepository.update(userPojo.getUserNo(), updateUserPojo) == 0) {
            new RuntimeException("비밀번호 초기화에 실패했음"); // TODO: 예외 후처리 필요
        }
    }

    public List<RefreshTokenDetailResponse> getTokens(int userNo) {
        List<Tokens> tokensPojo = tokensRepository.findAllActiveTokensByUserNo(userNo);
        return tokensPojo.stream()
            .map(row -> RefreshTokenDetailResponse.builder()
                .refreshToken(row.getRefreshToken())
                .clientOs(row.getClientOs())
                .createdAt(row.getCreatedAt())
                .build()
            ).toList();
    }

    public AccessTokenResponse refreshAccessToken(int userNo, String refreshToken) {
        // 토큰 검토
        
        // TODO: 토큰 발급

        // TODO: 토큰 DB 반영

        return AccessTokenResponse.builder().build();
    }

    public void deleteToken(int userNo, String refreshToken) {
        Tokens tokenPojo = tokensRepository.findByRefreshToken(refreshToken).orElseThrow(() -> 
            new RuntimeException("토큰을 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        if (tokenPojo.getUserNo() != userNo) {
            throw new RuntimeException("사용자가 일치하지 않음"); // TODO: 예외 후처리 필요
        }

        if (tokensRepository.deleteByRefreshToken(refreshToken) == 0) {
            throw new RuntimeException("토큰을 삭제하는데 실패함."); // TODO: 예외 후처리 필요
        }
    }

}
