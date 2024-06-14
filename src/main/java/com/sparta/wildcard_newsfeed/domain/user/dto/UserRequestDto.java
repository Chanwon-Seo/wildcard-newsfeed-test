package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserRequestDto {

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 Email", example = "aaa@gmail.com")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@(?:[a-zA-Z]+\\.)+[a-zA-Z]{2,7}$", message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "한 줄 소개", example = "안녕하세요!")
    private String introduce;

    @Schema(description = "현재 비밀번호", example = "currentPWD12@@")
    @Size(min = 10, message = "비밀번호는 최소 10글자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).*$", message = "비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해야 합니다.")
    private String password;

    @Schema(description = "바꿀 비밀번호", example = "changePWD77!!")
    @Size(min = 10, message = "비밀번호는 최소 10글자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).*$", message = "비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해야 합니다.")
    private String changePassword;

    public void encryptPassword(String encryptedPassword) {
        this.changePassword = encryptedPassword;
    }

    public User toUserDomain() {
        return User.builder()
                .name(name)
                .email(email)
                .introduce(introduce)
                .password(changePassword)
                .build();
    }
}