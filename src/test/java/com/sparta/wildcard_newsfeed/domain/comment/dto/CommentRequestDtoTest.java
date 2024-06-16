package com.sparta.wildcard_newsfeed.domain.comment.dto;

import com.sparta.wildcard_newsfeed.domain.user.dto.UserEmailRequestDto;
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
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Comment request Dto Test")
class CommentRequestDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("댓글 성공")
    void comment_success() {
        // given
        CommentRequestDto requestDto = CommentRequestDto.builder()
                .content("content")
                .build();

        // when
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("댓글_실패 - 내용 미입력")
    void comment_Bad_Request() {
        // given
        CommentRequestDto requestDto = CommentRequestDto.builder()
//                .content("content")
                .build();

        // when
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(requestDto);

        // then
        assertThat(violations).isNotEmpty();
        for (ConstraintViolation<CommentRequestDto> violation : violations) {
            assertEquals("댓글 내용은 필수 입력 값입니다.", violation.getMessage());
        }
    }


}