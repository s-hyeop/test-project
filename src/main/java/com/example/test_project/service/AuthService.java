package com.example.test_project.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.example.test_project.security.CustomUserDetails;
import com.example.test_project.security.JwtTokenProvider;
import com.example.test_project.util.AuthUtil;
import com.example.test_project.util.EmailUtil;
import com.example.test_project.util.RedisUtil;
import com.example.test_project.util.UuidUtil;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final TokensRepository tokensRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;
    private final EmailUtil emailUtil;
    private final RedisUtil redisUtil;

    @Value("${jwt.access-expiration-minutes}")
    private int accessExpirationMinutes;

    @Value("${jwt.refresh-expiration-minutes}")
    private int refreshExpirationMinutes;

    public EmailExistResponse existsByEmail(EmailRequest emailRequest) {
        // 계정이 없으면 true, 계정이 있으면 false
        boolean result = usersRepository.findByEmail(emailRequest.getEmail()).isPresent();

        return EmailExistResponse.builder()
            .exists(result)
            .build();
    }

    public AccessTokenResponse login(LoginRequest loginRequest, String ClientOs) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(auth);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal(); 

        String accessToken = jwt.generateAccessToken(customUserDetails.getUserNo(), customUserDetails.getEmail(), customUserDetails.getRole());
        String refreshToken = UuidUtil.generateUuidV7();

        Tokens tokenPojo = new Tokens();
        tokenPojo.setUserNo(customUserDetails.getUserNo());
        tokenPojo.setRefreshToken(refreshToken);
        tokenPojo.setClientOs(ClientOs); // TODO: ...
        tokenPojo.setAccessTokenExpiresAt(LocalDateTime.now().plus(accessExpirationMinutes, ChronoUnit.MINUTES));
        tokenPojo.setRefreshTokenExpiresAt(LocalDateTime.now().plus(refreshExpirationMinutes, ChronoUnit.MINUTES));
        tokenPojo.setCreatedAt(LocalDateTime.now());

        if (tokensRepository.save(tokenPojo) == null) {
            throw new RuntimeException("토큰 할당에 실패");
        }

        return AccessTokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public void sendSignupCode(SignupCodeSendRequest signupCodeSendRequest) {
        // 계정이 존재하지 않는지 확인
        if (!usersRepository.findByEmail(signupCodeSendRequest.getEmail()).isEmpty()) {
            throw new RuntimeException("이미 사용중인 이메일."); // TODO: 예외 후처리 필요
        }

        // 인증 코드 생성
        String code = AuthUtil.generateSixDigitCode();

        // 이메일 발송
        try {
            emailUtil.sendEmail(signupCodeSendRequest.getEmail(), "회원가입 이메일 인증", code);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송에 실패 함");
        }

        // redis 에 추가
        redisUtil.saveSignupKey(signupCodeSendRequest.getEmail(), code);
    }

    public void verifySignupCode(SignupCodeVerifyRequest signupCodeVerifyRequest) {
        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifySignupKey(signupCodeVerifyRequest.getEmail(), signupCodeVerifyRequest.getCode())) {
            throw new RuntimeException("인증 번호가 올바르지 않음");
        }
    }

    public void signup(SignupRequest signupRequest) {
        // 계정이 존재하지 않는지 확인
        if (!usersRepository.findByEmail(signupRequest.getEmail()).isEmpty()) {
            throw new RuntimeException("이미 사용중인 이메일."); // TODO: 예외 후처리 필요
        }

        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifySignupKey(signupRequest.getEmail(), signupRequest.getCode())) {
            throw new RuntimeException("인증 번호가 올바르지 않음");
        }

        // redis 검증 삭제
        redisUtil.deleteSignupKey(signupRequest.getEmail());

        // 회원 추가 pojo 생성
        Users userPojo = new Users();
        userPojo.setEmail(signupRequest.getEmail());
        userPojo.setPassword(signupRequest.getPassword());
        userPojo.setUserName(signupRequest.getUserName());
        userPojo.setRole("USRE");
        userPojo.setCreatedAt(LocalDateTime.now());

        // 회원 추가 실패 확인
        if (usersRepository.save(userPojo) == null) {
            throw new RuntimeException("회원 추가에 실패함."); // TODO: 예외 후처리 필요
        }
    }

    public void sendResetPasswordCode(ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {
        // 계정이 존재하는지 확인
        usersRepository.findByEmail(resetPasswordCodeSendRequest.getEmail()).orElseThrow(() ->
            new RuntimeException("존재하지 않는 이메일.")  // TODO: 예외 후처리 필요
        );

        // 인증 코드 생성
        String code = AuthUtil.generateSixDigitCode();

        // 이메일 발송
        try {
            emailUtil.sendEmail(resetPasswordCodeSendRequest.getEmail(), "비밀번호 찾기 이메일 인증", code);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송에 실패 함");
        }

        // redis 에 추가
        redisUtil.saveResetPasswordKey(resetPasswordCodeSendRequest.getEmail(), code);
    }

    public void verifyResetPasswordCode(ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {
        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifyResetPasswordKey(resetPasswordCodeVerifyRequest.getEmail(), resetPasswordCodeVerifyRequest.getCode())) {
            throw new RuntimeException("인증 번호가 올바르지 않음");
        }
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // 계정이 존재하는지 확인
        Users userPojo = usersRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(() ->
            new RuntimeException("존재하지 않는 이메일.")  // TODO: 예외 후처리 필요
        );

        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifyResetPasswordKey(resetPasswordRequest.getEmail(), resetPasswordRequest.getCode())) {
            throw new RuntimeException("인증 번호가 올바르지 않음");
        }

        // redis 검증 삭제
        redisUtil.deleteResetPasswordKey(resetPasswordRequest.getEmail());

        // 새로운 비밀번호 해싱
        String hashPassword = passwordEncoder.encode(resetPasswordRequest.getPassword());

        // 변경할 비밀번호 pojo에 반영
        Users updateUserPojo = new Users();
        updateUserPojo.setPassword(hashPassword);

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

    public AccessTokenResponse refreshAccessToken(String refreshToken) {
        Tokens tokenPojo = tokensRepository.findByRefreshToken(refreshToken).orElseThrow(() -> 
            new RuntimeException("토큰을 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        Users userPojo = usersRepository.find(tokenPojo.getUserNo()).orElseThrow(() -> 
            new RuntimeException("사용자를 찾을 수 없음") // TODO: 예외 후처리 필요
        );

        Tokens updateTokenPojo = new Tokens();
        updateTokenPojo.setAccessTokenExpiresAt(LocalDateTime.now().plus(accessExpirationMinutes, ChronoUnit.MINUTES));

        if (tokensRepository.update(tokenPojo.getTokenNo(), updateTokenPojo) == 0) {
            throw new RuntimeException("토큰 할당에 실패");
        }

        String accessToken = jwt.generateAccessToken(userPojo.getUserNo(), userPojo.getEmail(), userPojo.getRole());

        return AccessTokenResponse.builder()
            .accessToken(accessToken)
            .build();
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
