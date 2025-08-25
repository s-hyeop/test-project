package com.example.test_project.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
import lombok.extern.slf4j.Slf4j;


/**
 * 인증 관련 비즈니스 로직 서비스
 * 
 * <p>회원가입, 로그인, 비밀번호 재설정, 토큰 관리 등의 인증 관련 비즈니스 로직을 처리합니다.
 * JWT 토큰 발급 및 관리, 이메일 인증 코드 처리를 담당합니다.</p>
 */
@Slf4j
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


    /**
     * 이메일 존재 여부를 확인합니다.
     * 
     * @param emailRequest 확인할 이메일 정보
     * @return 이메일 존재 여부 (true: 존재, false: 미존재)
     */
    @Transactional(readOnly = true)
    public EmailExistResponse existsByEmail(EmailRequest emailRequest) {
        log.debug("이메일 존재 여부 확인 시작 - email: {}", emailRequest.getEmail());

        boolean result = usersRepository.findByEmail(emailRequest.getEmail()).isPresent();

        log.debug("이메일 존재 여부 확인 완료 - email: {}, exists: {}", emailRequest.getEmail(), result);

        return EmailExistResponse.builder()
                .exists(result)
                .build();
    }


    /**
     * 사용자 로그인을 처리하고 JWT 토큰을 발급합니다.
     * 
     * <p>Spring Security를 통해 인증을 수행하고,
     * 성공 시 Access Token과 Refresh Token을 생성합니다.</p>
     * 
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @param clientOs 클라이언트 OS 정보
     * @return Access Token과 Refresh Token
     * @throws UnauthorizedException 이메일 또는 비밀번호가 잘못된 경우
     * @throws InternalServerException 토큰 생성에 실패한 경우
     */
    @Transactional
    public AccessTokenResponse login(LoginRequest loginRequest, String clientOs) {
        log.debug("로그인 처리 시작 - email: {}, clientOs: {}", loginRequest.getEmail(), clientOs);

        try {
            // Spring Security 인증 처리
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(auth);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

            log.debug("인증 성공 - userNo: {}, email: {}, role: {}", customUserDetails.getUserNo(), customUserDetails.getEmail(), customUserDetails.getRole());

            // JWT 토큰 생성
            String accessToken = jwt.generateAccessToken(customUserDetails.getUserNo(), customUserDetails.getEmail(), customUserDetails.getRole());
            String refreshToken = UuidUtil.generateUuidV7();

            log.debug("토큰 생성 완료 - refreshToken: {}", refreshToken);

            // 토큰 정보 저장
            Tokens tokenPojo = new Tokens();
            tokenPojo.setUserNo(customUserDetails.getUserNo());
            tokenPojo.setRefreshToken(refreshToken);
            tokenPojo.setClientOs(clientOs);
            tokenPojo.setAccessTokenExpiresAt(LocalDateTime.now().plus(appProperties.getJwtAccessExpirationMinutes(), ChronoUnit.MINUTES));
            tokenPojo.setRefreshTokenExpiresAt(LocalDateTime.now().plus(appProperties.getJwtRefreshExpirationMinutes(), ChronoUnit.MINUTES));
            tokenPojo.setCreatedAt(LocalDateTime.now());

            if (tokensRepository.save(tokenPojo) == null) {
                log.error("토큰 저장 실패 - userNo: {}", customUserDetails.getUserNo());
                throw new InternalServerException("토큰 생성에 실패했습니다.");
            }

            log.info("로그인 성공 - userNo: {}, email: {}", customUserDetails.getUserNo(), customUserDetails.getEmail());

            return AccessTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증 정보 - email: {}", loginRequest.getEmail());
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }


    /**
     * 회원가입용 인증 코드를 생성하고 이메일로 발송합니다.
     * 
     * <p>6자리 인증 코드를 생성하여 이메일로 발송하고,
     * Redis에 5분간 임시 저장합니다.</p>
     * 
     * @param signupCodeSendRequest 인증 코드를 받을 이메일 정보
     * @throws ConflictException 이미 사용 중인 이메일인 경우
     * @throws InternalServerException 이메일 발송에 실패한 경우
     */
    @Transactional(readOnly = true)
    public void sendSignupCode(SignupCodeSendRequest signupCodeSendRequest) {
        String email = signupCodeSendRequest.getEmail();
        log.debug("회원가입 인증 코드 발송 시작 - email: {}", email);

        // 이메일 중복 확인
        if (!usersRepository.findByEmail(email).isEmpty()) {
            log.warn("회원가입 인증 코드 발송 실패 - 이미 사용 중인 이메일 - email: {}", email);
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        // 인증 코드 생성
        String code = AuthUtil.generateSixDigitCode();
        log.debug("인증 코드 생성 완료 - email: {}", email);

        // 이메일 발송
        try {
            emailUtil.sendEmail(email, "회원가입 이메일 인증", code);
            log.debug("인증 코드 이메일 발송 완료 - email: {}", email);
        } catch (MessagingException e) {
            log.error("인증 코드 이메일 발송 실패 - email: {}, error: {}", email, e.getMessage());
            throw new InternalServerException("이메일 발송에 실패했습니다.");
        }

        // Redis에 저장
        redisUtil.saveSignupKey(email, code);
        log.info("회원가입 인증 코드 발송 성공 - email: {}", email);
    }


    /**
     * 회원가입용 인증 코드를 검증합니다.
     * 
     * @param signupCodeVerifyRequest 이메일과 인증 코드 정보
     * @throws BadRequestException 인증 코드가 올바르지 않은 경우
     */
    @Transactional(readOnly = true)
    public void verifySignupCode(SignupCodeVerifyRequest signupCodeVerifyRequest) {
        String email = signupCodeVerifyRequest.getEmail();
        log.debug("회원가입 인증 코드 검증 시작 - email: {}", email);

        if (!redisUtil.verifySignupKey(email, signupCodeVerifyRequest.getCode())) {
            log.warn("회원가입 인증 코드 검증 실패 - email: {}", email);
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }

        log.info("회원가입 인증 코드 검증 성공 - email: {}", email);
    }


    /**
     * 회원가입을 처리합니다.
     * 
     * <p>인증 코드 검증 후 새로운 사용자 계정을 생성합니다.
     * 비밀번호는 bcrypt로 해싱하여 저장됩니다.</p>
     * 
     * @param signupRequest 회원가입 정보
     * @throws ConflictException 이미 사용 중인 이메일인 경우
     * @throws BadRequestException 인증 코드가 올바르지 않은 경우
     * @throws InternalServerException 회원가입에 실패한 경우
     */
    @Transactional
    public void signup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        log.debug("회원가입 처리 시작 - email: {}, userName: {}", email, signupRequest.getUserName());

        // 이메일 중복 확인
        if (!usersRepository.findByEmail(email).isEmpty()) {
            log.warn("회원가입 실패 - 이미 사용 중인 이메일 - email: {}", email);
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        // 인증 코드 검증
        if (!redisUtil.verifySignupKey(email, signupRequest.getCode())) {
            log.warn("회원가입 실패 - 잘못된 인증 코드 - email: {}", email);
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }

        // Redis 검증 삭제
        redisUtil.deleteSignupKey(email);

        // 비밀번호 해싱
        String hashPassword = passwordEncoder.encode(signupRequest.getPassword());
        log.debug("비밀번호 해싱 완료 - email: {}", email);

        // 회원 정보 저장
        Users userPojo = new Users();
        userPojo.setEmail(email);
        userPojo.setPassword(hashPassword);
        userPojo.setUserName(signupRequest.getUserName());
        userPojo.setRole("USER");
        userPojo.setCreatedAt(LocalDateTime.now());

        if (usersRepository.save(userPojo) == null) {
            log.error("회원 저장 실패 - email: {}", email);
            throw new InternalServerException("회원가입에 실패했습니다.");
        }

        log.info("회원가입 성공 - email: {}, userName: {}", email, signupRequest.getUserName());
    }


    /**
     * 비밀번호 재설정용 인증 코드를 생성하고 이메일로 발송합니다.
     * 
     * @param resetPasswordCodeSendRequest 인증 코드를 받을 이메일 정보
     * @throws NotFoundException 존재하지 않는 이메일인 경우
     * @throws InternalServerException 이메일 발송에 실패한 경우
     */
    @Transactional(readOnly = true)
    public void sendResetPasswordCode(ResetPasswordCodeSendRequest resetPasswordCodeSendRequest) {
        String email = resetPasswordCodeSendRequest.getEmail();
        log.debug("비밀번호 재설정 인증 코드 발송 시작 - email: {}", email);

        // 계정 존재 확인
        usersRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("비밀번호 재설정 인증 코드 발송 실패 - 존재하지 않는 이메일 - email: {}", email);
            return new NotFoundException("존재하지 않는 이메일입니다.");
        });

        // 인증 코드 생성
        String code = AuthUtil.generateSixDigitCode();
        log.debug("인증 코드 생성 완료 - email: {}", email);

        // 이메일 발송
        try {
            emailUtil.sendEmail(email, "비밀번호 찾기 이메일 인증", code);
            log.debug("인증 코드 이메일 발송 완료 - email: {}", email);
        } catch (MessagingException e) {
            log.error("인증 코드 이메일 발송 실패 - email: {}, error: {}", email, e.getMessage());
            throw new InternalServerException("이메일 발송에 실패했습니다.");
        }

        // Redis에 저장
        redisUtil.saveResetPasswordKey(email, code);
        log.info("비밀번호 재설정 인증 코드 발송 성공 - email: {}", email);
    }


    /**
     * 비밀번호 재설정용 인증 코드를 검증합니다.
     * 
     * @param resetPasswordCodeVerifyRequest 이메일과 인증 코드 정보
     * @throws BadRequestException 인증 코드가 올바르지 않은 경우
     */
    @Transactional(readOnly = true)
    public void verifyResetPasswordCode(ResetPasswordCodeVerifyRequest resetPasswordCodeVerifyRequest) {
        String email = resetPasswordCodeVerifyRequest.getEmail();
        log.debug("비밀번호 재설정 인증 코드 검증 시작 - email: {}", email);

        if (!redisUtil.verifyResetPasswordKey(email, resetPasswordCodeVerifyRequest.getCode())) {
            log.warn("비밀번호 재설정 인증 코드 검증 실패 - email: {}", email);
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }

        log.info("비밀번호 재설정 인증 코드 검증 성공 - email: {}", email);
    }


    /**
     * 비밀번호를 재설정합니다.
     * 
     * <p>인증 코드 검증 후 새로운 비밀번호로 변경합니다.</p>
     * 
     * @param resetPasswordRequest 비밀번호 재설정 정보
     * @throws NotFoundException 존재하지 않는 이메일인 경우
     * @throws BadRequestException 인증 코드가 올바르지 않은 경우
     * @throws InternalServerException 비밀번호 재설정에 실패한 경우
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail();
        log.debug("비밀번호 재설정 시작 - email: {}", email);

        // 계정 존재 확인
        Users userPojo = usersRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("비밀번호 재설정 실패 - 존재하지 않는 이메일 - email: {}", email);
            return new NotFoundException("존재하지 않는 이메일입니다.");
        });

        // 인증 코드 검증
        if (!redisUtil.verifyResetPasswordKey(email, resetPasswordRequest.getCode())) {
            log.warn("비밀번호 재설정 실패 - 잘못된 인증 코드 - email: {}", email);
            throw new BadRequestException("인증번호가 올바르지 않습니다.");
        }

        // Redis 검증 삭제
        redisUtil.deleteResetPasswordKey(email);

        // 새로운 비밀번호 해싱
        String hashPassword = passwordEncoder.encode(resetPasswordRequest.getPassword());
        log.debug("새 비밀번호 해싱 완료 - email: {}", email);

        // 비밀번호 업데이트
        Users updateUserPojo = new Users();
        updateUserPojo.setPassword(hashPassword);

        if (usersRepository.update(userPojo.getUserNo(), updateUserPojo) == 0) {
            log.error("비밀번호 업데이트 실패 - userNo: {}", userPojo.getUserNo());
            throw new InternalServerException("비밀번호 재설정에 실패했습니다.");
        }

        log.info("비밀번호 재설정 성공 - email: {}", email);
    }


    /**
     * 사용자의 활성 토큰 목록을 조회합니다.
     * 
     * <p>여러 기기에서 로그인한 경우 각 기기별 토큰 정보를 반환합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @return 활성 Refresh Token 목록
     */
    @Transactional(readOnly = true)
    public List<RefreshTokenDetailResponse> getTokens(int userNo) {
        log.debug("토큰 목록 조회 시작 - userNo: {}", userNo);

        List<Tokens> tokensPojo = tokensRepository.findAllActiveTokensByUserNo(userNo);

        List<RefreshTokenDetailResponse> result = tokensPojo.stream()
            .map(row -> RefreshTokenDetailResponse.builder()
                .refreshToken(row.getRefreshToken())
                .clientOs(row.getClientOs())
                .createdAt(row.getCreatedAt())
                .build()
            ).toList();

        log.debug("토큰 목록 조회 완료 - userNo: {}, 토큰 수: {}", userNo, result.size());

        return result;
    }


    /**
     * Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     * 
     * <p>Access Token 만료 임박 시 Refresh Token으로 새로운 Access Token을 발급합니다.
     * 갱신 임계값 이전에는 갱신이 제한됩니다.</p>
     * 
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token
     * @throws NotFoundException 유효하지 않은 토큰이거나 사용자를 찾을 수 없는 경우
     * @throws ConflictException 아직 갱신할 수 없는 경우
     * @throws BadRequestException Refresh Token이 만료된 경우
     * @throws InternalServerException 토큰 갱신에 실패한 경우
     */
    @Transactional
    public AccessTokenResponse refreshAccessToken(String refreshToken) {
        log.debug("Access Token 갱신 시작 - refreshToken: {}", refreshToken);

        // Refresh Token 확인
        Tokens tokenPojo = tokensRepository.findByRefreshToken(refreshToken).orElseThrow(() -> {
            log.warn("Access Token 갱신 실패 - 유효하지 않은 Refresh Token");
            return new NotFoundException("유효하지 않은 토큰입니다.");
        });

        // 사용자 정보 조회
        Users userPojo = usersRepository.find(tokenPojo.getUserNo()).orElseThrow(() -> {
            log.error("Access Token 갱신 실패 - 사용자 조회 실패 - userNo: {}", tokenPojo.getUserNo());
            return new NotFoundException("사용자를 찾을 수 없습니다.");
        });

        LocalDateTime now = LocalDateTime.now();

        // 갱신 가능 시간 확인
        if (now.isBefore(tokenPojo.getAccessTokenExpiresAt()
                .minusMinutes(appProperties.getJwtAccessTokenReissueThresholdMinutes()))) {
            log.warn("Access Token 갱신 거부 - 아직 갱신 시간이 아님 - userNo: {}", userPojo.getUserNo());
            throw new ConflictException("아직 갱신할 수 없습니다.");
        }

        // Refresh Token 만료 확인
        if (now.isAfter(tokenPojo.getRefreshTokenExpiresAt())) {
            log.warn("Access Token 갱신 실패 - Refresh Token 만료 - userNo: {}", userPojo.getUserNo());
            throw new BadRequestException("로그인 토큰이 만료되었습니다.");
        }

        // Access Token 만료 시간 업데이트
        Tokens updateTokenPojo = new Tokens();
        updateTokenPojo.setAccessTokenExpiresAt(LocalDateTime.now().plus(appProperties.getJwtAccessExpirationMinutes(), ChronoUnit.MINUTES));

        if (tokensRepository.update(tokenPojo.getTokenNo(), updateTokenPojo) == 0) {
            log.error("Access Token 갱신 실패 - 토큰 업데이트 실패 - tokenNo: {}", tokenPojo.getTokenNo());
            throw new InternalServerException("토큰 갱신에 실패했습니다.");
        }

        // 새로운 Access Token 생성
        String accessToken = jwt.generateAccessToken(userPojo.getUserNo(), userPojo.getEmail(), userPojo.getRole());
        log.info("Access Token 갱신 성공 - userNo: {}, email: {}", userPojo.getUserNo(), userPojo.getEmail());

        return AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }


    /**
     * Refresh Token을 삭제합니다.
     * 
     * <p>로그아웃 시 해당 세션의 Refresh Token을 삭제합니다.
     * 본인의 토큰만 삭제할 수 있습니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param refreshToken 삭제할 Refresh Token
     * @throws NotFoundException 토큰을 찾을 수 없는 경우
     * @throws ForbiddenException 토큰 삭제 권한이 없는 경우
     * @throws InternalServerException 토큰 삭제에 실패한 경우
     */
    @Transactional
    public void deleteToken(int userNo, String refreshToken) {
        log.debug("토큰 삭제 시작 - userNo: {}, refreshToken: {}", userNo, refreshToken);

        // 토큰 조회
        Tokens tokenPojo = tokensRepository.findByRefreshToken(refreshToken).orElseThrow(() -> {
            log.warn("토큰 삭제 실패 - 토큰을 찾을 수 없음 - userNo: {}", userNo);
            return new NotFoundException("토큰을 찾을 수 없습니다.");
        });

        // 권한 확인
        if (tokenPojo.getUserNo() != userNo) {
            log.warn("토큰 삭제 실패 - 권한 없음 - userNo: {}, tokenUserNo: {}", userNo, tokenPojo.getUserNo());
            throw new ForbiddenException("토큰 삭제 권한이 없습니다.");
        }

        // 토큰 삭제
        if (tokensRepository.deleteByRefreshToken(refreshToken) == 0) {
            log.error("토큰 삭제 실패 - DB 삭제 실패 - userNo: {}", userNo);
            throw new InternalServerException("토큰 삭제에 실패했습니다.");
        }

        log.info("토큰 삭제 성공 - userNo: {}", userNo);
    }

}
