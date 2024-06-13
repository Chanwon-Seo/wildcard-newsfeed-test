package com.sparta.wildcard_newsfeed.domain.user.entity;

import com.sparta.wildcard_newsfeed.domain.common.TimeStampEntity;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TimeStampEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usercode; // 사용자 ID
    private String password;
    private String name; // 이름
    private String email;
    private String introduce;
    @Setter
    private String refreshToken;
    @Enumerated(EnumType.STRING)
    private UserStatusEnum userStatus; //회원상태코드
    private LocalDateTime authUserAt; //상태 변경 시간

    @Enumerated(EnumType.STRING)
    private UserRoleEnum userRoleEnum;

    @Setter
    private String profileImageUrl;

    @Builder
    public User(String usercode, String password, String name, String email, String introduce, UserStatusEnum userStatus, LocalDateTime authUserAt, UserRoleEnum userRoleEnum) {
        this.usercode = usercode;
        this.password = password;
        this.name = name;
        this.email = email;
        this.introduce = introduce;
        this.userStatus = userStatus;
        this.authUserAt = authUserAt;
        this.userRoleEnum = userRoleEnum;
    }

    /**
     * 가입 시 사용
     **/
    public User(String usercode, String password, String email) {
        this.usercode = usercode;
        this.password = password;
        this.name = usercode;
        this.email = email;
        //유저 이름의 기본 값은 사용자 ID
        this.userStatus = UserStatusEnum.UNAUTHORIZED;
        this.authUserAt = LocalDateTime.now();
        this.userRoleEnum = UserRoleEnum.USER;
        //가입 시 회원상태코드는 정상과 상태 변경 시간 적용
    }

    /**
     * 회원 탈퇴 시 사용
     **/
    public void setUserStatus(UserStatusEnum userStatus) {
        this.userStatus = userStatus;
        this.authUserAt = LocalDateTime.now();
    }

    public void update(UserRequestDto requestDto) {
        this.password = requestDto.getChangePassword() != null ? requestDto.getChangePassword() : this.password;
        this.name = requestDto.getName() != null ? requestDto.getName() : this.name;
        this.email = requestDto.getEmail() != null ? requestDto.getEmail() : this.email;
        this.introduce = requestDto.getIntroduce() != null ? requestDto.getIntroduce() : this.introduce;
    }

    public void updateUserStatus() {
        this.userStatus = UserStatusEnum.ENABLED;
        this.authUserAt = LocalDateTime.now();
    }
}