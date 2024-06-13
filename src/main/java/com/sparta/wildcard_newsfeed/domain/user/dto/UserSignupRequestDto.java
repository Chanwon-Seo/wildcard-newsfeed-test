package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.*;
import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.SizeGroup;

@Getter
@Builder
@AllArgsConstructor
public class UserSignupRequestDto {

    @Schema(description = "사용자 ID", example = "userid1234")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", groups = PatternGroup.class,
            message = "대소문자 포함 영문 + 숫자만 입력해 주세요")
    @Size(min = 10, max = 20, groups = SizeGroup.class,
            message = "최소 10자 이상, 20자 이하로 입력해 주세요")
    @NotBlank(message = "아이디를 작성해주세요", groups = NotBlankGroup.class)
    private String usercode;

    @Schema(description = "사용자 이름", example = "currentPWD12@@")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).*$", groups = PatternGroup.class,
            message = "비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해 주세요")
    @Size(min = 10, groups = SizeGroup.class,
            message = "최소 10자 이상 입력해 주세요")
    @NotBlank(message = "비밀번호를 작성해주세요", groups = NotBlankGroup.class)
    private String password;

    @Schema(description = "사용자 이메일", example = "test@gmail.com")
    @Email(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", groups = PatternGroup.class,
            message = "이메일 형식에 맞지 않습니다.")
    @Size(max = 255, groups = SizeGroup.class,
            message = "이메일 입력 범위를 초과하였습니다.")
    @NotBlank(groups = NotBlankGroup.class,
            message = "이메일을 입력해주세요.")
    private String email;

}