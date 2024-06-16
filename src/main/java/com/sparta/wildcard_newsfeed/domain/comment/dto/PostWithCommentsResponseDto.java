package com.sparta.wildcard_newsfeed.domain.comment.dto;

import com.sparta.wildcard_newsfeed.domain.post.dto.PostResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostWithCommentsResponseDto {
    private PostResponseDto post;
    private List<CommentResponseDto> comments;

    public PostWithCommentsResponseDto(PostResponseDto post, List<CommentResponseDto> comments) {
        this.post = post;
        this.comments = comments;
    }
}