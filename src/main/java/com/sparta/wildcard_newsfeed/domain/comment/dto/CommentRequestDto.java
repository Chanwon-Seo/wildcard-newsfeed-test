package com.sparta.wildcard_newsfeed.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.wildcard_newsfeed.domain.comment.entity.Comment;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostPageRequestDto;
import com.sparta.wildcard_newsfeed.domain.post.entity.Post;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {

    @Schema(description = "댓글 내용", example = "내용")
    @NotBlank(message = "댓글 내용은 필수 입력 값입니다.", groups = NotBlankGroup.class)
    private String content;

    public Comment toDomainComment(User user, Post post) {
        return Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .likeCount(0L)
                .build();
    }
}