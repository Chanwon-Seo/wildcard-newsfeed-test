package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("UserEmail request Dto Test")
class UserEmailRequestDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("이메일 성공")
    void singup_success() {
        // given
        UserEmailRequestDto requestDto = UserEmailRequestDto.builder()
                .authCode(UUID.randomUUID().toString())
                .build();

        // when
        Set<ConstraintViolation<UserEmailRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일_실패 - 인증코드 미입력")
    void test1_1() {
        // given
        UserEmailRequestDto requestDto = UserEmailRequestDto.builder()
                .authCode(null)
                .build();

        // when
        Set<ConstraintViolation<UserEmailRequestDto>> violations = validator.validate(requestDto, ValidationGroups.NotBlankGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserEmailRequestDto> violation : violations) {
            assertEquals("인증 번호를 입력해 주세요", violation.getMessage());
        }
    }
}