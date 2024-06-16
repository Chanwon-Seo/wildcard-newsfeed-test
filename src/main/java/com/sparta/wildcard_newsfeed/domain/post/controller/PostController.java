package com.sparta.wildcard_newsfeed.domain.post.controller;

import com.sparta.wildcard_newsfeed.domain.comment.dto.CommentResponseDto;
import com.sparta.wildcard_newsfeed.domain.comment.dto.PostWithCommentsResponseDto;
import com.sparta.wildcard_newsfeed.domain.comment.service.CommentService;
import com.sparta.wildcard_newsfeed.domain.common.CommonResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostPageRequestDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostPageResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostRequestDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.repository.PostRepository;
import com.sparta.wildcard_newsfeed.domain.post.service.PostService;
import com.sparta.wildcard_newsfeed.exception.validation.ValidationSequence;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
@Tag(name = "Post 컨트롤러", description = "Post API")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    // 게시물 등록
    @PostMapping
    @Operation(summary = "게시물 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<PostResponseDto>> addPost(
            @AuthenticationPrincipal AuthenticationUser user,
            @Validated(ValidationSequence.class) @ModelAttribute PostRequestDto postRequestDto
    ) {
        PostResponseDto postResponseDto = postService.addPost(postRequestDto, user);
        return ResponseEntity.ok()
                .body(CommonResponseDto.<PostResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("게시물 등록 성공")
                        .data(postResponseDto)
                        .build());
    }

    // 게시물 전체 조회
    @GetMapping
    @Operation(summary = "게시물 전체 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 전체 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<Object>> findAll() {
        List<PostResponseDto> posts = postService.findAll();

        return ResponseEntity.ok()
                .body(CommonResponseDto.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("게시물 전체 조회 성공")
                        .data(posts)
                        .build());
    }

    //    // 게시물 단일 조회 + 해당 게시물에 달린 댓글 전체 조회
    @GetMapping("/{postId}")
    @Operation(summary = "게시물 단일 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 단일 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<PostWithCommentsResponseDto>> findById(@PathVariable(name = "postId") long id) {
        // 게시물 단일 조회
        PostResponseDto post = postService.findById(id);

        // 해당 게시물에 달린 댓글 전체 조회
        List<CommentResponseDto> comments = commentService.findAllCommentsByPostId(id);

        // Post와 Comments를 하나의 객체로 병합
        PostWithCommentsResponseDto postWithCommentsResponse = new PostWithCommentsResponseDto(post, comments);

        return ResponseEntity.ok()
                .body(CommonResponseDto.<PostWithCommentsResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("게시물 단일 조회, 댓글 조회 성공")
                        .data(postWithCommentsResponse)
                        .build());
    }

    //게시물 수정
    @PutMapping("/{postId}")
    @Operation(summary = "게시물 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<PostResponseDto>> updatePost(
            @AuthenticationPrincipal AuthenticationUser user,
            @Validated(ValidationSequence.class) @ModelAttribute PostRequestDto postRequestDto,
            @PathVariable Long postId
    ) {
        PostResponseDto postResponseDto = postService.updatePost(postRequestDto, postId, user);
        return ResponseEntity.ok()
                .body(CommonResponseDto.<PostResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("게시물 수정 성공")
                        .data(postResponseDto)
                        .build());
    }

    //게시물 삭제
    @DeleteMapping("/{postId}")
    @Operation(summary = "게시물 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<PostResponseDto>> deletePost(
            @AuthenticationPrincipal AuthenticationUser user,
            @Valid @PathVariable Long postId
    ) {
        postService.deletePost(postId, user);
        return ResponseEntity.ok()
                .body(CommonResponseDto.<PostResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("게시물 삭제 성공")
                        .build());
    }

    //페이지네이션
    @PostMapping("/page")
    @Operation(summary = "페이징")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "페이징 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<Page<PostPageResponseDto>>> getPostPage(@Valid @RequestBody PostPageRequestDto requestDto) {
        Page<PostPageResponseDto> page = postService.getPostPage(requestDto);
        return ResponseEntity.ok()
                .body(CommonResponseDto.<Page<PostPageResponseDto>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("게시물 페이지 조회 성공")
                        .data(page)
                        .build());
    }
}