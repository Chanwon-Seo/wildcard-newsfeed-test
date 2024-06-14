package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups;
import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.NotBlankGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class UserEmailRequestDto {

    @Schema(description = "이메일 인증번호", example = "")
    @NotBlank(message = "인증 번호를 입력해 주세요", groups = NotBlankGroup.class)
    private String authCode;
}