package com.sparta.wildcard_newsfeed.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.ConstructorProperties;
import java.util.List;

import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.NotBlankGroup;

@Data
@Builder
@NoArgsConstructor
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
}