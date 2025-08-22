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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(int userNo) {
        Users userPojo = usersRepository.find(userNo).orElseThrow(() ->
            new NotFoundException("회원을 찾을 수 없습니다.")
        );

        return UserDetailResponse.builder()
            .email(userPojo.getEmail())
            .userName(userPojo.getUserName())
            .createdAt(userPojo.getCreatedAt())
            .build();
    }

    @Transactional
    public void updateUser(int userNo, UserPatchRequest userPatchRequest) {
        usersRepository.find(userNo).orElseThrow(() ->
            new NotFoundException("회원을 찾을 수 없습니다.")
        );

        Users updateUserPojo = new Users();
        updateUserPojo.setUserName(userPatchRequest.getUserName());

        if (usersRepository.update(userNo, updateUserPojo) == 0) {
            throw new InternalServerException("회원 정보 수정에 실패했습니다.");
        }
    }

    @Transactional
    public void changePassword(int userNo, UserChangePasswordRequest userChangePasswordRequest) {
        Users userPojo = usersRepository.find(userNo).orElseThrow(() ->
            new NotFoundException("회원을 찾을 수 없습니다.")
        );
        

        if (!passwordEncoder.matches(userChangePasswordRequest.getPassword(), userPojo.getPassword())) {
            throw new BadRequestException("현재 비밀번호가 일치하지 않습니다.");
        }

        String hashPassword = passwordEncoder.encode(userChangePasswordRequest.getNewPassword());

        Users updateUserPojo = new Users();
        updateUserPojo.setPassword(hashPassword);

        if (usersRepository.update(userNo, updateUserPojo) == 0) {
            throw new InternalServerException("비밀번호 변경에 실패했습니다.");
        }
    }

}
