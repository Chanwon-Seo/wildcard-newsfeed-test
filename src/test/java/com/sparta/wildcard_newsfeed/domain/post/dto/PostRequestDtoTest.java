package com.sparta.wildcard_newsfeed.domain.post.dto;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;

import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
@DisplayName("Post request Dto Test")
class PostRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final FixtureMonkey sut = FixtureMonkey.builder()
            .defaultNotNull(Boolean.FALSE)
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build();

    @Test
    @RepeatedTest(value = 10)
    @DisplayName("게시물 등록_성공")
    void should_addPost_when_Success() {
        // given
        PostRequestDto actual = PostRequestDto.builder()
                .title("title")
                .content("content")
                .build();

        // when
        Set<ConstraintViolation<PostRequestDto>> violations = validator.validate(actual, NotBlankGroup.class);

        //then
        then(actual).isNotNull();
        for (ConstraintViolation<PostRequestDto> violation : violations) {
            if ("title".equals(violation.getPropertyPath().toString())) {
                assertEquals("제목은 필수 입력 값입니다.", violation.getMessage());
            } else if ("content".equals(violation.getPropertyPath().toString())) {
                assertEquals("내용은 필수 입력 값입니다.", violation.getMessage());
            }
        }
    }

    @Test
    @RepeatedTest(value = 10)
    @DisplayName("게시물 등록_실패 - 제목 또는 내용을 입력하지 않은 경우")
    void should_ThrowException_when_titleOrContentBlank() {
        // given
        PostRequestDto actual = sut.giveMeOne(PostRequestDto.class);

        // when
        Set<ConstraintViolation<PostRequestDto>> violations = validator.validate(actual, NotBlankGroup.class);

        //then
        then(actual).isNotNull();
        for (ConstraintViolation<PostRequestDto> violation : violations) {
            if ("title".equals(violation.getPropertyPath().toString())) {
                assertEquals("제목은 필수 입력 값입니다.", violation.getMessage());
            } else if ("content".equals(violation.getPropertyPath().toString())) {
                assertEquals("내용은 필수 입력 값입니다.", violation.getMessage());
            }
        }
    }

}