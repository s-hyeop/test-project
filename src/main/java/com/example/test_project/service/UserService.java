package com.example.test_project.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.tables.pojos.Users;
import com.example.test_project.config.exception.*;
import com.example.test_project.dto.request.*;
import com.example.test_project.dto.response.*;
import com.example.test_project.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 사용자 관리 비즈니스 로직 서비스
 * 
 * <p>사용자 정보 조회, 수정, 비밀번호 변경 등의 비즈니스 로직을 처리합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 상세 정보를 조회합니다.
     * 
     * @param userNo 사용자 번호
     * @return 사용자 상세 정보 (이메일, 사용자명, 가입일시)
     * @throws NotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(int userNo) {
        log.debug("사용자 정보 조회 시작 - userNo: {}", userNo);
        
        Users userPojo = usersRepository.find(userNo).orElseThrow(() -> {
            log.error("사용자 조회 실패 - userNo: {}", userNo);
            return new NotFoundException("회원을 찾을 수 없습니다.");
        });

        log.debug("사용자 정보 조회 성공 - userNo: {}, email: {}", userNo, userPojo.getEmail());
        
        return UserDetailResponse.builder()
                .email(userPojo.getEmail())
                .userName(userPojo.getUserName())
                .createdAt(userPojo.getCreatedAt())
                .build();
    }


    /**
     * 사용자 정보를 수정합니다.
     * 
     * <p>현재는 사용자명만 수정 가능합니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param userPatchRequest 사용자 정보 수정 요청
     * @throws NotFoundException 사용자를 찾을 수 없는 경우
     * @throws InternalServerException 정보 수정에 실패한 경우
     */
    @Transactional
    public void updateUser(int userNo, UserPatchRequest userPatchRequest) {
        log.debug("사용자 정보 수정 시작 - userNo: {}, userName: {}", userNo, userPatchRequest.getUserName());
        
        usersRepository.find(userNo).orElseThrow(() -> {
            log.error("사용자 조회 실패 - userNo: {}", userNo);
            return new NotFoundException("회원을 찾을 수 없습니다.");
        });

        Users updateUserPojo = new Users();
        updateUserPojo.setUserName(userPatchRequest.getUserName());

        int updateCount = usersRepository.update(userNo, updateUserPojo);
        if (updateCount == 0) {
            log.error("사용자 정보 수정 실패 - userNo: {}", userNo);
            throw new InternalServerException("회원 정보 수정에 실패했습니다.");
        }

        log.info("사용자 정보 수정 성공 - userNo: {}, userName: {}", userNo, userPatchRequest.getUserName());
    }


    /**
     * 사용자의 비밀번호를 변경합니다.
     * 
     * <p>현재 비밀번호를 확인한 후 새로운 비밀번호로 변경합니다.
     * 새 비밀번호는 bcrypt로 해싱하여 저장됩니다.</p>
     * 
     * @param userNo 사용자 번호
     * @param userChangePasswordRequest 비밀번호 변경 요청 (현재 비밀번호, 새 비밀번호)
     * @throws NotFoundException 사용자를 찾을 수 없는 경우
     * @throws BadRequestException 현재 비밀번호가 일치하지 않는 경우
     * @throws InternalServerException 비밀번호 변경에 실패한 경우
     */
    @Transactional
    public void changePassword(int userNo, UserChangePasswordRequest userChangePasswordRequest) {
        log.debug("비밀번호 변경 시작 - userNo: {}", userNo);

        Users userPojo = usersRepository.find(userNo).orElseThrow(() -> {
            log.error("사용자 조회 실패 - userNo: {}", userNo);
            return new NotFoundException("회원을 찾을 수 없습니다.");
        });

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(userChangePasswordRequest.getPassword(), userPojo.getPassword())) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치 - userNo: {}", userNo);
            throw new BadRequestException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 해싱
        String hashPassword = passwordEncoder.encode(userChangePasswordRequest.getNewPassword());
        log.debug("새 비밀번호 해싱 완료 - userNo: {}", userNo);

        Users updateUserPojo = new Users();
        updateUserPojo.setPassword(hashPassword);

        int updateCount = usersRepository.update(userNo, updateUserPojo);
        if (updateCount == 0) {
            log.error("비밀번호 변경 실패 - userNo: {}", userNo);
            throw new InternalServerException("비밀번호 변경에 실패했습니다.");
        }

        log.info("비밀번호 변경 성공 - userNo: {}", userNo);
    }

}
