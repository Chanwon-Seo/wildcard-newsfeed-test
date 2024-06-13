package com.sparta.wildcard_newsfeed.domain.user.dto;

import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserSignupResponseDto {
    private String usercode;
    private String email;

    public UserSignupResponseDto(User user) {
        usercode = user.getUsercode();
        email = user.getEmail();
    }

    public static UserSignupResponseDto of(User user) {
        return UserSignupResponseDto.builder()
                .usercode(user.getUsercode())
                .email(user.getEmail())
                .build();
    }
}