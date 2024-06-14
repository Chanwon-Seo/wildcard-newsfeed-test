package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class UserResponseDto {

    @Schema(description = "사용자 ID")
    private String usercode;
    @Schema(description = "사용자 이름")
    private String name;
    @Schema(description = "한 줄 소개")
    private String introduce;
    @Schema(description = "사용자 Email")
    private String email;
    @Schema(description = "프로필 사진 주소")
    private String profileImageUrl;



    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .usercode(user.getUsercode())
                .name(user.getName())
                .introduce(user.getIntroduce())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}