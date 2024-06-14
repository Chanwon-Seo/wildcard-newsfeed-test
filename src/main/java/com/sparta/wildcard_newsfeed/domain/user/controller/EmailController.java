package com.sparta.wildcard_newsfeed.domain.user.controller;

import com.sparta.wildcard_newsfeed.domain.common.CommonResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserEmailRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.service.AuthCodeService;
import com.sparta.wildcard_newsfeed.domain.user.service.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@Tag(name = "Email 컨트롤러", description = "email API")
public class EmailController {

    private final AuthCodeService authCodeService;

    @PostMapping("/verify")
    @Operation(summary = "이메일 인증")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<PostResponseDto>> verifyEmailAuth(
            @AuthenticationPrincipal AuthenticationUser loginUser,
            @Validated @RequestBody UserEmailRequestDto requestDto
    ) {
        authCodeService.verifyAuthCode(loginUser, requestDto);

        return ResponseEntity.ok()
                .body(CommonResponseDto.<PostResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("이메일 인증 성공")
                        .build());
    }
}