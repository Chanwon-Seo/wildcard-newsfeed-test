package com.sparta.wildcard_newsfeed.domain.user.controller;

import com.sparta.wildcard_newsfeed.domain.common.CommonResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.service.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User 컨트롤러", description = "user API")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<UserSignupResponseDto>> signup(
            @Validated(ValidationSequence.class) @RequestBody UserSignupRequestDto requestDto) {
        UserSignupResponseDto responseDto = userService.signup(requestDto);

        return ResponseEntity.ok()
                .body(CommonResponseDto.<UserSignupResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("회원가입 성공")
                        .data(responseDto)
                        .build());
    }

    @DeleteMapping("/resign")
    @Operation(summary = "회원탈퇴")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<UserSignupResponseDto>> resign(
            @AuthenticationPrincipal AuthenticationUser user,
            @Valid @RequestBody Map<String, String> map
    ) {
        userService.resign(user, map.get("password"));

        return ResponseEntity.ok()
                .body(CommonResponseDto.<UserSignupResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("회원탈퇴 성공")
                        .build());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "프로필 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<UserResponseDto>> getUser(@PathVariable Long userId) {
        UserResponseDto userResponseDto = userService.findById(userId);

        return ResponseEntity.ok()
                .body(CommonResponseDto.<UserResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("프로필 조회 성공")
                        .data(userResponseDto)
                        .build());
    }

    @PutMapping("/{userId}")
    @Operation(summary = "프로필 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<UserResponseDto>> updateUser(
            @AuthenticationPrincipal AuthenticationUser loginUser,
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        UserResponseDto userResponseDto = userService.updateUser(loginUser, userId, requestDto);

        return ResponseEntity.ok()
                .body(CommonResponseDto.<UserResponseDto>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("프로필 수정 성공")
                        .data(userResponseDto)
                        .build());
    }

    @PostMapping("/{userId}/profile-image")
    @Operation(summary = "프로필 사진 업로드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 사진 업로드 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)))
    })
    public ResponseEntity<CommonResponseDto<String>> uploadProfileImage(
            @AuthenticationPrincipal AuthenticationUser loginUser,
            @PathVariable Long userId,
            @RequestParam MultipartFile file
    ) {
        String savedS3Url = userService.uploadProfileImage(loginUser, userId, file);

        return ResponseEntity.ok()
                .body(CommonResponseDto.<String>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("프로필 사진 업로드 성공")
                        .data(savedS3Url)
                        .build());
    }
}