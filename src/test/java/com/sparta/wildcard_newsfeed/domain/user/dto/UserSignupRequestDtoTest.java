package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.NotBlankGroup;
import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.PatternGroup;
import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.SizeGroup;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("User SignUp Dto Test")
class UserSignupRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("회원가입_성공")
    void singup_success() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isEmpty();
    }

    /**
     * 아이디 검증
     * test1_1 아이디 미입력
     * test1_2 아이디 입력 길이 조건 미달
     * test1_3 아이디 입력 조건 미달
     */
    @Test
    @DisplayName("회원가입_실패 - 아이디_미입력")
    void test1_1() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, NotBlankGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("아이디를 작성해주세요", violation.getMessage());
        }
    }

    @Test
    @DisplayName("회원가입_실패 - 아이디 입력 길이 조건 미달")
    void test1_2() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("11")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, SizeGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("최소 10자 이상, 20자 이하로 입력해 주세요", violation.getMessage());
        }
    }

    @Test
    @DisplayName("회원가입_실패 - 아이디 입력 조건 미달")
    void test1_3() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("@@")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, PatternGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("대소문자 포함 영문 + 숫자만 입력해 주세요", violation.getMessage());
        }
    }


    /**
     * 비밀번호 검증
     * test2_1 비밀번호 미입력
     * test2_2 비밀번호 입력 길이 조건 미달
     * test2_3 비밀번호 입력 조건 미달
     */
    @Test
    @DisplayName("회원가입_실패 - 비밀번호_미입력")
    void test2_1() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, NotBlankGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("비밀번호를 작성해주세요", violation.getMessage());
        }
    }

    @Test
    @DisplayName("회원가입_실패 - 비밀번호 입력 길이 조건 미달")
    void test2_2() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("1")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, SizeGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("최소 10자 이상 입력해 주세요", violation.getMessage());
        }
    }

    @Test
    @DisplayName("회원가입_실패 - 비밀번호 입력 조건 미달")
    void test2_3() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("abcd")
                .email("test@gmail.com")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, PatternGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해 주세요", violation.getMessage());
        }
    }

    /**
     * 이메일 검증
     * test3_1 이메일 미입력
     * test3_2 이메일 입력 길이 조건 미달
     * test3_3 이메일 입력 조건 미달
     */
    @Test
    @DisplayName("회원가입_실패 - 이메일_미입력")
    void test3_1() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, NotBlankGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("이메일을 입력해주세요.", violation.getMessage());
        }
    }

    @Test
    @DisplayName("회원가입_실패 - 이메일 입력 길이 조건 미달")
    void test3_2() {
        // given
        StringBuilder email = new StringBuilder();
        for (int i = 1; i <= 150; i++) {
            email.append(i);
        }

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email(email.toString())
                .build();
        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, SizeGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("이메일 입력 범위를 초과하였습니다.", violation.getMessage());
        }
    }

    @Test
    @DisplayName("회원가입_실패 - 이메일 입력 조건 미달")
    void test3_3() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("111111")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(requestDto, PatternGroup.class);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<UserSignupRequestDto> violation : violations) {
            assertEquals("이메일 형식에 맞지 않습니다.", violation.getMessage());
        }
    }


}