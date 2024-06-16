package com.sparta.wildcard_newsfeed.domain.post.dto;

import com.sparta.wildcard_newsfeed.domain.post.entity.Post;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.NotBlankGroup;

@Getter
@Builder
@AllArgsConstructor
public class PostRequestDto {

    @Schema(description = "게시물 제목", example = "제목")
    @NotBlank(message = "제목은 필수 입력 값입니다.", groups = NotBlankGroup.class)
    private String title;

    @Schema(description = "게시물 내용", example = "내용")
    @NotBlank(message = "내용은 필수 입력 값입니다.", groups = NotBlankGroup.class)
    private String content;

    @Schema(description = "업로드 파일")
    private List<MultipartFile> files;

    public Post toDomainPost(User user) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .likeCount(0L)
                .build();
    }
}