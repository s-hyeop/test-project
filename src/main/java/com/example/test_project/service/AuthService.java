package com.example.test_project.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.tables.pojos.Tokens;
import com.example.jooq.tables.pojos.Users;
import com.example.test_project.config.exception.*;
import com.example.test_project.config.properties.AppProperties;
import com.example.test_project.config.security.CustomUserDetails;
import com.example.test_project.config.security.provider.JwtTokenProvider;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.repository.TokensRepository;
import com.example.test_project.repository.UsersRepository;
import com.example.test_project.util.*;

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

    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public EmailExistResponse existsByEmail(EmailRequest emailRequest) {
        // 계정이 없으면 true, 계정이 있으면 false
        boolean result = usersRepository.findByEmail(emailRequest.getEmail()).isPresent();

        return EmailExistResponse.builder()
            .exists(result)
            .build();
    }

    @Transactional
    public AccessTokenResponse login(LoginRequest loginRequest, String ClientOs) {
        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(auth);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal(); 

            String accessToken = jwt.generateAccessToken(customUserDetails.getUserNo(), customUserDetails.getEmail(), customUserDetails.getRole());
            String refreshToken = UuidUtil.generateUuidV7();

            Tokens tokenPojo = new Tokens();
            tokenPojo.setUserNo(customUserDetails.getUserNo());
            tokenPojo.setRefreshToken(refreshToken);
            tokenPojo.setClientOs(ClientOs);
            tokenPojo.setAccessTokenExpiresAt(LocalDateTime.now().plus(appProperties.getJwtAccessExpirationMinutes(), ChronoUnit.MINUTES));
            tokenPojo.setRefreshTokenExpiresAt(LocalDateTime.now().plus(appProperties.getJwtRefreshExpirationMinutes(), ChronoUnit.MINUTES));
            tokenPojo.setCreatedAt(LocalDateTime.now());

            if (tokensRepository.save(tokenPojo) == null) {
                throw new InternalServerException("토큰 생성에 실패했습니다.");
            }

            return AccessTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void sendSignupCode(SignupCodeSendRequest signupCodeSendRequest) {
        // 계정이 존재하지 않는지 확인
        if (!usersRepository.findByEmail(signupCodeSendRequest.getEmail()).isEmpty()) {
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        // 인증 코드 생성
        String code = AuthUtil.generateSixDigitCode();

        // 이메일 발송
        try {
            emailUtil.sendEmail(signupCodeSendRequest.getEmail(), "회원가입 이메일 인증", code);
        } catch (MessagingException e) {
            throw new InternalServerException("이메일 발송에 실패했습니다.");
        }

        // redis 에 추가
        redisUtil.saveSignupKey(signupCodeSendRequest.getEmail(), code);
    }

    @Transactional(readOnly = true)
    public void verifySignupCode(SignupCodeVerifyRequest signupCodeVerifyRequest) {
        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifySignupKey(signupCodeVerifyRequest.getEmail(), signupCodeVerifyRequest.getCode())) {
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public void signup(SignupRequest signupRequest) {
        // 계정이 존재하지 않는지 확인
        if (!usersRepository.findByEmail(signupRequest.getEmail()).isEmpty()) {
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifySignupKey(signupRequest.getEmail(), signupRequest.getCode())) {
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }

        // redis 검증 삭제
        redisUtil.deleteSignupKey(signupRequest.getEmail());

        String hashPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 회원 추가 pojo 생성
        Users userPojo = new Users();
        userPojo.setEmail(signupRequest.getEmail());
        userPojo.setPassword(hashPassword);
        userPojo.setUserName(signupRequest.getUserName());
        userPojo.setRole("USER");
        userPojo.setCreatedAt(LocalDateTime.now());

        // 회원 추가 실패 확인
        if (usersRepository.save(userPojo) == null) {
            throw new InternalServerException("회원가입에 실패했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void sendResetPasswordCode(ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {
        // 계정이 존재하는지 확인
        usersRepository.findByEmail(resetPasswordCodeSendRequest.getEmail()).orElseThrow(() ->
            new NotFoundException("존재하지 않는 이메일입니다.")
        );

        // 인증 코드 생성
        String code = AuthUtil.generateSixDigitCode();

        // 이메일 발송
        try {
            emailUtil.sendEmail(resetPasswordCodeSendRequest.getEmail(), "비밀번호 찾기 이메일 인증", code);
        } catch (MessagingException e) {
            throw new InternalServerException("이메일 발송에 실패했습니다.");
        }

        // redis 에 추가
        redisUtil.saveResetPasswordKey(resetPasswordCodeSendRequest.getEmail(), code);
    }

    @Transactional(readOnly = true)
    public void verifyResetPasswordCode(ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {
        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifyResetPasswordKey(resetPasswordCodeVerifyRequest.getEmail(), resetPasswordCodeVerifyRequest.getCode())) {
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // 계정이 존재하는지 확인
        Users userPojo = usersRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(() ->
            new NotFoundException("존재하지 않는 이메일입니다.")
        );

        // redis 이메일 인증 코드 검증
        if (!redisUtil.verifyResetPasswordKey(resetPasswordRequest.getEmail(), resetPasswordRequest.getCode())) {
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
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
            throw new InternalServerException("비밀번호 재설정에 실패했습니다.");
        }
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public AccessTokenResponse refreshAccessToken(String refreshToken) {
        Tokens tokenPojo = tokensRepository.findByRefreshToken(refreshToken).orElseThrow(() -> 
            new NotFoundException("유효하지 않은 토큰입니다.")
        );

        Users userPojo = usersRepository.find(tokenPojo.getUserNo()).orElseThrow(() -> 
            new NotFoundException("사용자를 찾을 수 없습니다.")
        );

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(tokenPojo.getAccessTokenExpiresAt().minusMinutes(appProperties.getJwtAccessTokenReissueThresholdMinutes()))) {
            throw new ConflictException("아직 갱신할 수 없습니다.");
        }

        if (now.isAfter(tokenPojo.getRefreshTokenExpiresAt())) {
            throw new BadRequestException("로그인 토큰이 만료되었습니다.");
        }

        Tokens updateTokenPojo = new Tokens();
        updateTokenPojo.setAccessTokenExpiresAt(LocalDateTime.now().plus(appProperties.getJwtAccessExpirationMinutes(), ChronoUnit.MINUTES));

        if (tokensRepository.update(tokenPojo.getTokenNo(), updateTokenPojo) == 0) {
            throw new InternalServerException("토큰 갱신에 실패했습니다.");
        }

        String accessToken = jwt.generateAccessToken(userPojo.getUserNo(), userPojo.getEmail(), userPojo.getRole());

        return AccessTokenResponse.builder()
            .accessToken(accessToken)
            .build();
    }

    @Transactional
    public void deleteToken(int userNo, String refreshToken) {
        Tokens tokenPojo = tokensRepository.findByRefreshToken(refreshToken).orElseThrow(() -> 
            new NotFoundException("토큰을 찾을 수 없습니다.")
        );

        if (tokenPojo.getUserNo() != userNo) {
            throw new ForbiddenException("토큰 삭제 권한이 없습니다.");
        }

        if (tokensRepository.deleteByRefreshToken(refreshToken) == 0) {
            throw new InternalServerException("토큰 삭제에 실패했습니다.");
        }
    }

}
